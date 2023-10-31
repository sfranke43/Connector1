/*
 *  Copyright (c) 2021 Microsoft Corporation
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Microsoft Corporation - initial API and implementation
 *       Mercedes Benz Tech Innovation - add toggles for proxy behavior
 *
 */

package org.eclipse.edc.connector.dataplane.http.pipeline;

import org.eclipse.edc.connector.dataplane.http.params.HttpRequestFactory;
import org.eclipse.edc.connector.dataplane.http.spi.HttpRequestParamsProvider;
import org.eclipse.edc.connector.dataplane.spi.pipeline.DataSource;
import org.eclipse.edc.connector.dataplane.spi.pipeline.DataSourceFactory;
import org.eclipse.edc.spi.http.EdcHttpClient;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.result.Result;
import org.eclipse.edc.spi.types.domain.HttpDataAddress;
import org.eclipse.edc.spi.types.domain.transfer.DataFlowRequest;
import org.jetbrains.annotations.NotNull;
import org.eclipse.edc.connector.transfer.spi.store.TransferProcessStore;
import org.eclipse.edc.connector.transfer.spi.types.TransferProcess;

import static org.eclipse.edc.spi.types.domain.HttpDataAddress.HTTP_DATA;

/**
 * Instantiates {@link HttpDataSource}s for requests whose source data type is {@link HttpDataAddress#HTTP_DATA}.
 */
public class HttpDataSourceFactory implements DataSourceFactory {

    private final EdcHttpClient httpClient;
    private final HttpRequestParamsProvider requestParamsProvider;
    private final Monitor monitor;
    private final HttpRequestFactory requestFactory;
	//new for thesis
	private final TransferProcessStore transferProcessStore;

    public HttpDataSourceFactory(EdcHttpClient httpClient, HttpRequestParamsProvider requestParamsProvider, Monitor monitor, HttpRequestFactory requestFactory, TransferProcessStore transferProcessStore) {
        this.httpClient = httpClient;
        this.requestParamsProvider = requestParamsProvider;
        this.monitor = monitor;
        this.requestFactory = requestFactory;
		this.transferProcessStore = transferProcessStore;
    }

    @Override
    public boolean canHandle(DataFlowRequest request) {
        return HTTP_DATA.equals(request.getSourceDataAddress().getType());
    }

    @Override
    public @NotNull Result<Boolean> validate(DataFlowRequest request) {
        return validateRequest(request).map(it -> true);
    }

    @Override
    public @NotNull Result<Void> validateRequest(DataFlowRequest request) {
        try {
            createSource(request);
        } catch (Exception e) {
            return Result.failure("Failed to build HttpDataSource: " + e.getMessage());
        }
        return Result.success();
    }

    @Override
    public DataSource createSource(DataFlowRequest request) {
		System.out.println("creating data source");
		System.out.println(request.getProcessId());
		TransferProcess transferProcess = this.transferProcessStore.findById(request.getId());
		TransferProcess transferProcess1 = this.transferProcessStore.findForCorrelationId(request.getProcessId());
        var dataAddress = HttpDataAddress.Builder.newInstance()
                .copyFrom(request.getSourceDataAddress())
                .build();
        String processId = request.getProcessId();

        //ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
        return HttpDataSource.Builder.newInstance()
                .httpClient(httpClient)
                .monitor(monitor)
                .requestId(request.getId())
                .name(dataAddress.getName())
                .params(requestParamsProvider.provideSourceParams(request))
                .requestFactory(requestFactory)
                .build();
    }
}
