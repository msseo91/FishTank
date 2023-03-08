package com.marineseo.fishtank.jpa

import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy
import org.hibernate.boot.model.naming.Identifier
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment

class FishNamingStrategy: CamelCaseToUnderscoresNamingStrategy() {
    override fun toPhysicalTableName(name: Identifier, jdbcEnvironment: JdbcEnvironment): Identifier {
        return Identifier(name.text, name.isQuoted)
    }

    override fun toPhysicalColumnName(name: Identifier, jdbcEnvironment: JdbcEnvironment): Identifier {
        return Identifier(name.text, name.isQuoted)
    }
}

fun String.toCamelCase(): String {
    val result = StringBuilder()
    var nextIsUpper = false
    for (ch in this) {
        if (ch == '_') {
            nextIsUpper = true
        } else {
            if (nextIsUpper) {
                result.append(ch.uppercaseChar())
                nextIsUpper = false
            } else {
                result.append(ch.lowercaseChar())
            }
        }
    }
    return result.toString()
}