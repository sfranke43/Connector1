/*
 *  Copyright (c) 2023 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG) - initial API and implementation
 *
 */

package org.eclipse.edc.connector.api.management.transferprocess.validation;

import jakarta.json.JsonObject;
import org.eclipse.edc.api.validation.DataAddressValidator;
import org.eclipse.edc.validator.jsonobject.JsonObjectValidator;
import org.eclipse.edc.validator.jsonobject.validators.MandatoryObject;
import org.eclipse.edc.validator.jsonobject.validators.MandatoryValue;
import org.eclipse.edc.validator.jsonobject.validators.OptionalIdNotBlank;
import org.eclipse.edc.validator.spi.Validator;

import static org.eclipse.edc.connector.transfer.spi.types.TransferRequest.TRANSFER_REQUEST_ASSET_ID;
import static org.eclipse.edc.connector.transfer.spi.types.TransferRequest.TRANSFER_REQUEST_CONNECTOR_ADDRESS;
import static org.eclipse.edc.connector.transfer.spi.types.TransferRequest.TRANSFER_REQUEST_CONNECTOR_ID;
import static org.eclipse.edc.connector.transfer.spi.types.TransferRequest.TRANSFER_REQUEST_CONTRACT_ID;
import static org.eclipse.edc.connector.transfer.spi.types.TransferRequest.TRANSFER_REQUEST_DATA_DESTINATION;
import static org.eclipse.edc.connector.transfer.spi.types.TransferRequest.TRANSFER_REQUEST_PROTOCOL;

public class TransferRequestValidator {

    public static Validator<JsonObject> instance() {
        return JsonObjectValidator.newValidator()
                .verifyId(OptionalIdNotBlank::new)
                .verify(TRANSFER_REQUEST_CONNECTOR_ADDRESS, MandatoryValue::new)
                .verify(TRANSFER_REQUEST_CONTRACT_ID, MandatoryValue::new)
                .verify(TRANSFER_REQUEST_PROTOCOL, MandatoryValue::new)
                .verify(TRANSFER_REQUEST_CONNECTOR_ID, MandatoryValue::new)
                .verify(TRANSFER_REQUEST_ASSET_ID, MandatoryValue::new)
                .verify(TRANSFER_REQUEST_DATA_DESTINATION, MandatoryObject::new)
                .verifyObject(TRANSFER_REQUEST_DATA_DESTINATION, DataAddressValidator::instance)
                .build();
    }
}
