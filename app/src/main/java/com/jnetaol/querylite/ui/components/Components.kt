package com.jnetaol.querylite.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jnetaol.querylite.ui.theme.*

@Composable
fun GlowButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    color: Color = AccentPrimary
) {
    val alpha = if (enabled) 1f else 0.4f
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, color.copy(alpha = alpha)),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = color.copy(alpha = alpha)
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}

@Composable
fun NeonCard(
    modifier: Modifier = Modifier,
    glowColor: Color = AccentPrimary.copy(alpha = 0.15f),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
            .padding(1.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            Column { content() }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = AccentPrimary,
            fontWeight = FontWeight.Bold
        )
        if (action != null) action()
    }
}

@Composable
fun EmptyState(
    icon: ImageVector = Icons.Default.Info,
    title: String,
    subtitle: String = "",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = TextSecondary.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, color = TextSecondary)
        if (subtitle.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextDisabled)
        }
    }
}

@Composable
fun StatusBadge(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = StatusInfo
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun DataGrid(
    columns: List<String>,
    rows: List<Map<String, String?>>,
    modifier: Modifier = Modifier
) {
    val horizontalScrollState = rememberScrollState()

    Column(modifier = modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(8.dp))
        .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
    ) {
        Box(modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(horizontalScrollState)
        ) {
            Column {
                // Header row
                Row(modifier = Modifier.background(DarkSurfaceVariant)) {
                    columns.forEach { col ->
                        Box(
                            modifier = Modifier
                                .width(140.dp)
                                .padding(horizontal = 8.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = col,
                                color = AccentPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Divider(color = DarkBorder, thickness = 1.dp)

                // Data rows
                rows.forEachIndexed { index, row ->
                    Row(modifier = Modifier.background(if (index % 2 == 0) DarkSurface else DarkBackground)) {
                        columns.forEach { col ->
                            Box(
                                modifier = Modifier
                                    .width(140.dp)
                                    .padding(horizontal = 8.dp, vertical = 8.dp)
                            ) {
                                val value = row[col]
                                Text(
                                    text = value ?: "NULL",
                                    color = if (value == null) TextDisabled else TextPrimary,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                    if (index < rows.size - 1) {
                        Divider(color = DarkBorder.copy(alpha = 0.5f), thickness = 0.5.dp)
                    }
                }

                if (rows.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No data", color = TextDisabled, fontSize = 13.sp)
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(horizontalScrollState)
        ) {
            Spacer(modifier = Modifier.width(1.dp))
        }
    }
}

// SQL Syntax Highlighting
private val SQL_KEYWORDS = setOf(
    "SELECT", "FROM", "WHERE", "INSERT", "INTO", "VALUES", "UPDATE", "SET",
    "DELETE", "CREATE", "TABLE", "DROP", "ALTER", "ADD", "COLUMN", "INDEX",
    "VIEW", "TRIGGER", "JOIN", "LEFT", "RIGHT", "INNER", "OUTER", "CROSS",
    "ON", "AND", "OR", "NOT", "IN", "LIKE", "BETWEEN", "IS", "NULL",
    "ORDER", "BY", "GROUP", "HAVING", "ASC", "DESC", "LIMIT", "OFFSET",
    "AS", "DISTINCT", "ALL", "UNION", "EXCEPT", "INTERSECT", "EXISTS",
    "CASE", "WHEN", "THEN", "ELSE", "END", "IF", "BEGIN", "COMMIT",
    "ROLLBACK", "TRANSACTION", "PRAGMA", "EXPLAIN", "QUERY", "PLAN",
    "INTEGER", "TEXT", "REAL", "BLOB", "NUMERIC", "BOOLEAN",
    "PRIMARY", "KEY", "FOREIGN", "REFERENCES", "UNIQUE", "CHECK",
    "DEFAULT", "AUTOINCREMENT", "WITHOUT", "ROWID", "REPLACE",
    "ABORT", "FAIL", "IGNORE", "RECURSIVE", "TEMP", "TEMPORARY",
    "VACUUM", "ATTACH", "DETACH", "REINDEX", "ANALYZE",
    "GLOB", "REGEXP", "MATCH", "ESCAPE", "COLLATE",
    "ASC", "DESC", "NULLS", "FIRST", "LAST", "CURRENT_DATE",
    "CURRENT_TIME", "CURRENT_TIMESTAMP", "COUNT"
)

private val SQL_FUNCTIONS = setOf(
    "COUNT", "SUM", "AVG", "MIN", "MAX", "GROUP_CONCAT", "TOTAL",
    "ABS", "COALESCE", "IFNULL", "LENGTH", "LOWER", "UPPER",
    "SUBSTR", "REPLACE", "TRIM", "LTRIM", "RTRIM", "ROUND",
    "RANDOM", "HEX", "ZEROBLOB", "TYPEOF", "INSTR",
    "LIKE", "GLOB", "CAST", "DATE", "TIME", "DATETIME",
    "STRFTIME", "JULIANDAY", "UNIXEPOCH"
)

@Composable
fun SqlEditor(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "SQL Query",
    placeholder: String = "SELECT * FROM ..."
) {
    val highlightedText = remember(value.text) {
        buildHighlightedSql(value.text)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
            .background(DarkSurfaceVariant)
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .heightIn(min = 100.dp, max = 300.dp),
            textStyle = TextStyle(
                color = Color.Transparent,
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                lineHeight = 20.sp
            ),
            cursorBrush = SolidColor(AccentPrimary),
            decorationBox = { innerTextField ->
                Box {
                    Text(
                        text = highlightedText,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                    innerTextField()
                }
            }
        )
    }
}

private fun buildHighlightedSql(text: String) = buildAnnotatedString {
    val words = Regex("""('[^']*'|"[^"]*"|--[^\n]*|/\*[\s\S]*?\*/|\b\w+\b|[^\w\s])""")
    val matches = words.findAll(text)

    for (match in matches) {
        val token = match.value
        val startIndex = match.range.first

        append(text, start = lastIndex, end = match.range.first)

        val style = when {
            token.startsWith("'") || token.startsWith("\"") -> SpanStyle(color = SqlString)
            token.startsWith("--") || token.startsWith("/*") -> SpanStyle(color = SqlComment)
            token.uppercase() in SQL_KEYWORDS -> SpanStyle(color = SqlKeyword, fontWeight = FontWeight.Bold)
            token.uppercase() in SQL_FUNCTIONS -> SpanStyle(color = SqlFunction, fontWeight = FontWeight.Bold)
            token.toDoubleOrNull() != null -> SpanStyle(color = SqlNumber)
            token in setOf("=", "<", ">", "<=", ">=", "<>", "!=", ",", ";", "(", ")", "*", "+", "-", "/") ->
                SpanStyle(color = SqlOperator)
            else -> SpanStyle(color = TextPrimary)
        }
        withStyle(style) {
            append(token)
        }
    }
}
