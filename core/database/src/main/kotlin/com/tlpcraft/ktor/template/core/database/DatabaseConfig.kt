package com.tlpcraft.ktor.template.core.database

/** Connection settings for the application's relational database. */
public data class DatabaseConfig(
    public val jdbcUrl: String,
    public val username: String,
    public val password: String,
    public val maximumPoolSize: Int = DEFAULT_POOL_SIZE
) {
    override fun toString(): String = "DatabaseConfig(jdbcUrl=$jdbcUrl, username=$username, password=***, maximumPoolSize=$maximumPoolSize)"

    public companion object {
        public const val DEFAULT_POOL_SIZE: Int = 10
    }
}
