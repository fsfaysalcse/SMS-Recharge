package com.faysal.smsautomation.Models

data class Company(
    val company_name: String,
    val company_title: String,
    val license_duration: String,
    val license_start: String,
    val logo: String,
    val mitload: String,
    val remaining: Int,
    val reseller_limit: String,
    val sms_limit: String,
    val sms_use_count: String,
    val total_reseller: String,
    val user_limit: String,
    val version: String
)