/*
 *  Copyright (c) 2020 - 2022 Microsoft Corporation
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Microsoft Corporation - initial API and implementation
 *       SAP SE - bugfix (pass correct lease id for deletion)
 *
 */

package org.eclipse.edc.sql.lease;


import org.eclipse.edc.spi.persistence.LeaseContext;
import org.eclipse.edc.sql.QueryExecutor;
import org.eclipse.edc.transaction.spi.TransactionContext;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Duration;
import java.util.Objects;
import java.util.UUID;

/**
 * SQL-based implementation of the LeaseContext.
 * Acquiring a lease is implemented by adding an entry into the "lease" table in the database
 * Breaking a lease is implemented by deleting the respective entry
 */
public class SqlLeaseContext implements LeaseContext {
    private static final long DEFAULT_LEASE_DURATION = 60_000;
    private final TransactionContext trxContext;
    private final LeaseStatements statements;
    private final String leaseHolder;
    private final Connection connection;
    private final Clock clock;
    private final Duration leaseDuration;
    private final QueryExecutor queryExecutor;

    SqlLeaseContext(TransactionContext trxContext, LeaseStatements statements, String leaseHolder, Clock clock, Duration leaseDuration, Connection connection, QueryExecutor queryExecutor) {
        this.trxContext = trxContext;
        this.statements = statements;
        this.leaseHolder = leaseHolder;
        this.clock = clock;
        this.leaseDuration = leaseDuration;
        this.connection = connection;
        this.queryExecutor = queryExecutor;
    }

    @Override
    public void breakLease(String entityId) {
        trxContext.execute(() -> {

            var l = getLease(entityId);

            if (l != null) {
                if (!Objects.equals(leaseHolder, l.getLeasedBy())) {
                    throw new IllegalStateException("Current runtime does not hold the lease for Object (id [" + entityId + "]), cannot break lease!");
                }

                var stmt = statements.getDeleteLeaseTemplate();
                queryExecutor.execute(connection, stmt, l.getLeaseId());
            }
        });
    }

    @Override
    public void acquireLease(String entityId) {
        trxContext.execute(() -> {
            var now = clock.millis();

            var lease = getLease(entityId);

            if (lease != null && !lease.isExpired(clock)) {
                throw new IllegalStateException("Entity is currently leased!");
            }

            //clean out old lease if present
            if (lease != null) {
                var deleteStmt = statements.getDeleteLeaseTemplate();
                queryExecutor.execute(connection, deleteStmt, lease.getLeaseId());
            }

            // create new lease in DB
            var id = UUID.randomUUID().toString();
            var duration = leaseDuration != null ? leaseDuration.toMillis() : DEFAULT_LEASE_DURATION;
            var stmt = statements.getInsertLeaseTemplate();
            queryExecutor.execute(connection, stmt, id, leaseHolder, now, duration);

            //update entity with lease -> effectively lease entity
            var updStmt = statements.getUpdateLeaseTemplate();
            queryExecutor.execute(connection, updStmt, id, entityId);

        });
    }

    /**
     * Fetches a lease for a particular entity
     *
     * @param entityId The leased entity's ID (NOT the leaseID!)
     * @return The respective lease, or null of entity is not leased.
     */
    public @Nullable SqlLease getLease(String entityId) {
        var stmt = statements.getFindLeaseByEntityTemplate();
        return queryExecutor.single(connection, false, this::mapLease, stmt, entityId);
    }

    private SqlLease mapLease(ResultSet resultSet) throws SQLException {
        var lease = new SqlLease(resultSet.getString(statements.getLeasedByColumn()),
                resultSet.getLong(statements.getLeasedAtColumn()),
                resultSet.getLong(statements.getLeaseDurationColumn()));
        lease.setLeaseId(resultSet.getString(statements.getLeaseIdColumn()));
        return lease;
    }
}
