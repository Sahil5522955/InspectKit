package dev.inspectkit

import androidx.sqlite.db.SupportSQLiteDatabase

fun SupportSQLiteDatabase.asInspectKitSource(name: String): DatabaseSource {
    return DatabaseSource(
        name = name,
        tables = {
            query("SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' ORDER BY name").use { cursor ->
                buildList {
                    while (cursor.moveToNext()) {
                        add(cursor.getString(0))
                    }
                }
            }
        },
        query = { tableName ->
            val safeTableName = tableName.replace("\"", "\"\"")
            query("SELECT * FROM \"$safeTableName\" LIMIT 100").use { cursor ->
                val columns = cursor.columnNames.toList()
                val rows = buildList {
                    while (cursor.moveToNext()) {
                        add(
                            columns.indices.map { index ->
                                if (cursor.isNull(index)) "null" else cursor.getString(index)
                            }
                        )
                    }
                }
                QueryResult(columns, rows)
            }
        }
    )
}
