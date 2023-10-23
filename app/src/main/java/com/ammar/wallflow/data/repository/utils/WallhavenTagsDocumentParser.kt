package com.ammar.wallflow.data.repository.utils

import com.ammar.wallflow.data.network.model.wallhaven.NetworkWallhavenTag
import java.net.URL
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jsoup.nodes.Document

object WallhavenTagsDocumentParser {
    internal fun parsePopularTags(doc: Document): List<NetworkWallhavenTag> {
        val tagListEle = doc.select("div#taglist").first() ?: return emptyList()
        val tagMainElements = tagListEle.select("div.taglist-tagmain")
        val tags = tagMainElements.fold(ArrayList<NetworkWallhavenTag>()) { targetList, ele ->
            val tagNameSpan = ele.selectFirst("span.taglist-name") ?: return@fold targetList
            val nameAnchorEle = tagNameSpan.selectFirst("a")
            val href = nameAnchorEle?.attr("href") ?: return@fold targetList
            val id = URL(href).path
                .split('/')
                .getOrNull(2)
                ?.toLongOrNull()
                ?: return@fold targetList
            val name = tagNameSpan.text()
            val purity = when {
                nameAnchorEle.hasClass("nsfw") -> "nsfw"
                nameAnchorEle.hasClass("sketchy") -> "sketchy"
                else -> "sfw"
            }
            val categoryChainEle = ele.selectFirst("span.taglist-category")
            val categoryEle = categoryChainEle?.select("a")?.last()
            val category = categoryEle?.text() ?: ""
            val categoryId = categoryEle?.attr("href")?.let {
                URL(it).path
                    .split('/')
                    .getOrNull(2)
                    ?.toLongOrNull()
            } ?: 0
            val creatorEle = ele.selectFirst("span.taglist-creator")
            val createdAtStr = creatorEle?.selectFirst("time")?.attr("datetime")
            val createdAt = createdAtStr?.let { Instant.parse(it) } ?: Clock.System.now()
            targetList.add(
                NetworkWallhavenTag(
                    id = id,
                    name = name,
                    alias = "",
                    category_id = categoryId,
                    category = category,
                    purity = purity,
                    created_at = createdAt,
                ),
            )
            targetList
        }
        return tags
    }
}
