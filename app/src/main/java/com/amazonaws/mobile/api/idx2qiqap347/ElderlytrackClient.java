/*
 * Copyright 2010-2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.amazonaws.mobile.api.idx2qiqap347;

import com.amazonaws.mobile.api.idx2qiqap347.model.Empty;


@com.amazonaws.mobileconnectors.apigateway.annotation.Service(endpoint = "https://x2qiqap347.execute-api.ap-southeast-1.amazonaws.com/api")
public interface ElderlytrackClient {


    /**
     * A generic invoker to invoke any API Gateway endpoint.
     * @param request
     * @return ApiResponse
     */
    com.amazonaws.mobileconnectors.apigateway.ApiResponse execute(com.amazonaws.mobileconnectors.apigateway.ApiRequest request);
    
    /**
     * 
     * 
     * @return Empty
     */
    @com.amazonaws.mobileconnectors.apigateway.annotation.Operation(path = "/v1/beacon/disable_beacon", method = "POST")
    Empty v1BeaconDisableBeaconPost();
    
    /**
     * 
     * 
     * @return Empty
     */
    @com.amazonaws.mobileconnectors.apigateway.annotation.Operation(path = "/v1/beacon/load_distinctUUID", method = "GET")
    Empty v1BeaconLoadDistinctUUIDGet();
    
    /**
     * 
     * 
     * @return Empty
     */
    @com.amazonaws.mobileconnectors.apigateway.annotation.Operation(path = "/v1/resident/missing", method = "GET")
    Empty v1ResidentMissingGet();
    
    /**
     * 
     * 
     * @return Empty
     */
    @com.amazonaws.mobileconnectors.apigateway.annotation.Operation(path = "/v1/resident/relatives", method = "POST")
    Empty v1ResidentRelativesPost();
    
    /**
     * 
     * 
     * @return Empty
     */
    @com.amazonaws.mobileconnectors.apigateway.annotation.Operation(path = "/v1/resident/report_found", method = "POST")
    Empty v1ResidentReportFoundPost();
    
    /**
     * 
     * 
     * @return Empty
     */
    @com.amazonaws.mobileconnectors.apigateway.annotation.Operation(path = "/v1/resident/report_missing", method = "POST")
    Empty v1ResidentReportMissingPost();
    
    /**
     * 
     * 
     * @return Empty
     */
    @com.amazonaws.mobileconnectors.apigateway.annotation.Operation(path = "/v1/user/login_anonymous", method = "GET")
    Empty v1UserLoginAnonymousGet();
    
    /**
     * 
     * 
     * @return Empty
     */
    @com.amazonaws.mobileconnectors.apigateway.annotation.Operation(path = "/v1/user/login_with_email", method = "POST")
    Empty v1UserLoginWithEmailPost();




}

