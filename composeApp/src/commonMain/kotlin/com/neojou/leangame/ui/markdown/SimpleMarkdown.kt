package com.neojou.leangame.ui.markdown

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp

/**
 * Small Markdown subset for command docs: headings, lists, fenced code, **bold**, `code`.
 */
@Composable
fun SimpleMarkdown(
    markdown: String,
    modifier: Modifier = Modifier,
) {
    val blocks = remember(markdown) { splitBlocks(markdown) }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        blocks.forEach { block ->
            when (block) {
                is MdBlock.Heading -> Text(
                    text = inlineMarkdown(block.text),
                    style = when (block.level) {
                        1 -> MaterialTheme.typography.titleLarge
                        2 -> MaterialTheme.typography.titleMedium
                        else -> MaterialTheme.typography.titleSmall
                    },
                    fontWeight = FontWeight.SemiBold,
                )
                is MdBlock.Paragraph -> Text(
                    text = inlineMarkdown(block.text),
                    style = MaterialTheme.typography.bodyMedium,
                )
                is MdBlock.Bullet -> Text(
                    text = buildAnnotatedString {
                        append("• ")
                        append(inlineMarkdown(block.text))
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 8.dp),
                )
                is MdBlock.Code -> Text(
                    text = block.text,
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceContainerHighest,
                            RoundedCornerShape(6.dp),
                        )
                        .padding(10.dp),
                )
            }
        }
    }
}

private sealed class MdBlock {
    data class Heading(val level: Int, val text: String) : MdBlock()
    data class Paragraph(val text: String) : MdBlock()
    data class Bullet(val text: String) : MdBlock()
    data class Code(val text: String) : MdBlock()
}

private fun splitBlocks(src: String): List<MdBlock> {
    val lines = src.replace("\r\n", "\n").lines()
    val out = mutableListOf<MdBlock>()
    val para = StringBuilder()
    var i = 0
    fun flushPara() {
        val t = para.toString().trim()
        if (t.isNotEmpty()) out += MdBlock.Paragraph(t)
        para.clear()
    }
    while (i < lines.size) {
        val line = lines[i]
        when {
            line.trimStart().startsWith("```") -> {
                flushPara()
                i++
                val code = StringBuilder()
                while (i < lines.size && !lines[i].trimStart().startsWith("```")) {
                    if (code.isNotEmpty()) code.append('\n')
                    code.append(lines[i])
                    i++
                }
                out += MdBlock.Code(code.toString())
            }
            line.startsWith("### ") -> {
                flushPara()
                out += MdBlock.Heading(3, line.removePrefix("### ").trim())
            }
            line.startsWith("## ") -> {
                flushPara()
                out += MdBlock.Heading(2, line.removePrefix("## ").trim())
            }
            line.startsWith("# ") -> {
                flushPara()
                out += MdBlock.Heading(1, line.removePrefix("# ").trim())
            }
            line.trim().startsWith("- ") || line.trim().startsWith("* ") -> {
                flushPara()
                val t = line.trim().removePrefix("- ").removePrefix("* ").trim()
                out += MdBlock.Bullet(t)
            }
            line.isBlank() -> flushPara()
            else -> {
                if (para.isNotEmpty()) para.append(' ')
                para.append(line.trim())
            }
        }
        i++
    }
    flushPara()
    return out
}

private fun inlineMarkdown(text: String): AnnotatedString = buildAnnotatedString {
    var i = 0
    while (i < text.length) {
        when {
            text.startsWith("**", i) -> {
                val end = text.indexOf("**", i + 2)
                if (end < 0) {
                    append(text[i])
                    i++
                } else {
                    withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                        append(text.substring(i + 2, end))
                    }
                    i = end + 2
                }
            }
            text[i] == '`' -> {
                val end = text.indexOf('`', i + 1)
                if (end < 0) {
                    append('`')
                    i++
                } else {
                    withStyle(SpanStyle(fontFamily = FontFamily.Monospace)) {
                        append(text.substring(i + 1, end))
                    }
                    i = end + 1
                }
            }
            else -> {
                append(text[i])
                i++
            }
        }
    }
}
