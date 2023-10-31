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
 *       Siemens AG - changes to make it compatible with AWS S3, Azure blob and ALI Object Storage presigned URL for upload
 *
 */

package org.eclipse.edc.connector.dataplane.http;

import org.eclipse.edc.connector.dataplane.http.params.HttpRequestFactory;
import org.eclipse.edc.connector.dataplane.http.params.HttpRequestParamsProviderImpl;
import org.eclipse.edc.connector.dataplane.http.pipeline.HttpDataSinkFactory;
import org.eclipse.edc.connector.dataplane.http.pipeline.HttpDataSourceFactory;
import org.eclipse.edc.connector.dataplane.http.spi.HttpRequestParamsProvider;
import org.eclipse.edc.connector.dataplane.spi.pipeline.DataTransferExecutorServiceContainer;
import org.eclipse.edc.connector.dataplane.spi.pipeline.PipelineService;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Provides;
import org.eclipse.edc.runtime.metamodel.annotation.Setting;
import org.eclipse.edc.spi.http.EdcHttpClient;
import org.eclipse.edc.spi.security.Vault;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.spi.types.TypeManager;
import org.eclipse.edc.connector.transfer.spi.store.TransferProcessStore;

/**
 * Provides support for reading data from an HTTP endpoint and sending data to an HTTP endpoint.
 */
@Provides(HttpRequestParamsProvider.class)
@Extension(value = DataPlaneHttpExtension.NAME)
public class DataPlaneHttpExtension implements ServiceExtension {
    public static final String NAME = "Data Plane HTTP";
    private static final int DEFAULT_PART_SIZE = 5;

    @Setting
    private static final String EDC_DATAPLANE_HTTP_SINK_PARTITION_SIZE = "edc.dataplane.http.sink.partition.size";

    @Inject
    private EdcHttpClient httpClient;

    @Inject
    private PipelineService pipelineService;

    @Inject
    private DataTransferExecutorServiceContainer executorContainer;

    @Inject
    private Vault vault;

    @Inject
    private TypeManager typeManager;
	
	//new for thesis
	@Inject
    private TransferProcessStore transferProcessStore;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        var monitor = context.getMonitor();
        var sinkPartitionSize = context.getSetting(EDC_DATAPLANE_HTTP_SINK_PARTITION_SIZE, DEFAULT_PART_SIZE);

        var paramsProvider = new HttpRequestParamsProviderImpl(vault, typeManager);
        context.registerService(HttpRequestParamsProvider.class, paramsProvider);

        var httpRequestFactory = new HttpRequestFactory();

        var sourceFactory = new HttpDataSourceFactory(httpClient, paramsProvider, monitor, httpRequestFactory, transferProcessStore);
        pipelineService.registerFactory(sourceFactory);

        var sinkFactory = new HttpDataSinkFactory(httpClient, executorContainer.getExecutorService(), sinkPartitionSize, monitor, paramsProvider, httpRequestFactory);
        pipelineService.registerFactory(sinkFactory);
    }

}
