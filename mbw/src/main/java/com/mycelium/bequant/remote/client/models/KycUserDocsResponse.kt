/**
* Auth API
* Auth API<br> <a href='/changelog'>Changelog</a>
*
* The version of the OpenAPI document: v0.0.50
* 
*
* NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
* https://openapi-generator.tech
* Do not edit the class manually.
*/
package com.mycelium.bequant.remote.client.models

import com.mycelium.bequant.remote.client.models.KycUserDocFile


import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 
 * @param address 
 * @param identity 
 * @param selfie 
 */

data class KycUserDocsResponse (
    @JsonProperty("address")
    val address: kotlin.collections.List<KycUserDocFile>,
    @JsonProperty("identity")
    val identity: kotlin.collections.List<KycUserDocFile>,
    @JsonProperty("selfie")
    val selfie: kotlin.collections.List<KycUserDocFile>
)
