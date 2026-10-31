package com.tlpcraft.ktor.template.core.model

/** A single validation failure: which input [field] is invalid and why. */
public data class FieldViolation(
    public val field: String,
    public val message: String
)
