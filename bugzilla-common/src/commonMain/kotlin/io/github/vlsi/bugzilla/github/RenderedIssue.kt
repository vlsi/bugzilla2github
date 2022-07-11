import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
class RenderedIssue(
    val title: String,
    val closedAt: Instant?,
    val markdown: String,
    val html: String,
    val labels: List<String>,
    val comments: List<RenderedComment>,
)

@Serializable
class RenderedComment(
    val created_at: Instant? = null,
    val markdown: String,
    val html: String,
)
