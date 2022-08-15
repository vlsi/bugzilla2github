package io.github.vlsi.bugzilla.ui

import RenderedIssue
import io.github.vlsi.bugzilla.dto.BugId
import kotlinx.coroutines.asPromise
import kotlinx.coroutines.async
import kotlinx.datetime.Clock
import kotlinx.js.jso
import react.*
import react.dom.html.InputType
import react.dom.html.ReactHTML
import react.dom.html.ReactHTML.br
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h1
import react.dom.html.ReactHTML.hr
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.label
import react.dom.html.ReactHTML.pre
import react.dom.html.ReactHTML.span
import react.query.QueryKey
import react.query.useQuery
import react.router.Outlet
import react.router.dom.Link
import react.router.useNavigate
import react.router.useParams

val SampleBugs by VFC {
    for (bug in listOf(5088, 42584, 66070, 65913, 65874, 65843, 65503, 65457)) {
        Link {
            to = "/bugs/$bug"
            +"Bug $bug"
        }
        +", "
    }
}

val BugBrowser by VFC {
    ReactHTML.h2 {
        +"Browse bugs"
    }

    var bugId by useState("")
    val navigate = useNavigate()

    div {
        SampleBugs()

        div {
            +"Bug id: "
            ReactHTML.input {
                type = InputType.text
                placeholder = "Bug ID"
                value = bugId
                onChange = {
                    val newStr = it.target.value
                    bugId = newStr
                    newStr.let { newId ->
                        bugId = newId
                        navigate("/bugs/$newId")
                        console.log("navigate to $newId")
                    }
                }
            }
        }
    }
    Outlet()
}

external interface BugQueryKey : QueryKey

fun BugQueryKey(bugId: BugId): BugQueryKey = arrayOf("bug", bugId.value.toString()).unsafeCast<BugQueryKey>()

inline var BugQueryKey.bugId: BugId
    get() = asDynamic()[1].unsafeCast<String>().toInt().let { BugId(it) }
    set(value) {
        asDynamic()[1] = value.value.toString()
    }

val BugInfo by VFC {
    val coroutineScope = useContext(CoroutineScopeContext)
    val bugService = useMemo { BugService(coroutineScope.coroutineContext) }
    val params = useParams()
    val bugId = params["bug_id"]!!.toInt().let { BugId(it) }

    val bugQuery = useQuery<_, Throwable, RenderedIssue, _>(BugQueryKey(bugId), { ctx ->
        val key = ctx.queryKey.unsafeCast<BugQueryKey>()
        val loadJob = coroutineScope.async {
            bugService.renderBugById(key.bugId)
        }
        ctx.signal.onabort = { loadJob.cancel() }
        loadJob.asPromise()
    })

    var showMarkdown by useState(false)
    var showHtml by useState(false)

    if (bugQuery.isLoading) {
        div {
            +"Loading..."
        }
    } else if (bugQuery.isError) {
        div {
            +"ERROR: ${bugQuery.error}"
        }
    } else {
        br()
        input {
            type = InputType.checkbox
            id = "showMarkdown"
            checked = showMarkdown
            onChange = {
                showMarkdown = it.target.checked
            }
        }
        +" "
        label {
            htmlFor = "showMarkdown"

            +"Show markdown"
        }
        br()
        input {
            type = InputType.checkbox
            id = "showHtml"
            checked = showHtml
            onChange = {
                showHtml = it.target.checked
            }
        }
        +" "
        label {
            htmlFor = "showHtml"

            +"Show HTML"
        }

        val issue = bugQuery.data!!
        h1 {
            +issue.title
        }
        if (issue.labels.isNotEmpty()) {
            div {
                +"Labels: "
                var isFirst = true
                for (label in issue.labels) {
                    if (!isFirst) {
                        +", "
                    }
                    isFirst = false
                    span {
                        style = jso {
                            backgroundColor = csstype.NamedColor.lightcoral
                        }
                        +label
                    }
                }
            }
        }
        showSource(showMarkdown, showHtml, issue.markdown, issue.html)
        div {
            dangerouslySetInnerHTML = jso {
                __html = issue.html
            }
        }
        for (comment in issue.comments) {
            hr()
            +"Comment at ${comment.created_at}:"
            showSource(showMarkdown, showHtml, comment.markdown, comment.html)
            div {
                dangerouslySetInnerHTML = jso {
                    __html = comment.html
                }
            }
        }
        div {
            +"Closed at ${issue.closedAt ?: Clock.System.now()}"
        }
    }
}

private fun ChildrenBuilder.showSource(
    showMarkdown: Boolean,
    showHtml: Boolean,
    markdown: String,
    html: String,
) {
    if (showMarkdown) {
        pre {
            style = jso {
                backgroundColor = csstype.NamedColor.lightgray
            }
            +markdown
        }
    }
    if (showMarkdown && showHtml) {
        div { +"=>" }
    }
    if (showHtml) {
        pre {
            style = jso {
                backgroundColor = csstype.NamedColor.lightgray
            }
            +html
        }
    }
}

