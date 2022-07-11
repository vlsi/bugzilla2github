package io.github.vlsi.bugzilla.ui

import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import react.Fragment
import react.create
import react.createContext
import react.dom.client.createRoot
import react.query.QueryClient
import react.query.QueryClientProvider

val CoroutineScopeContext = createContext<CoroutineScope>()

fun main() {
    val root = createRoot(document.getElementById("root")!!)
    val queryClient = QueryClient()
    root.render(
        Fragment.create {
            QueryClientProvider {
                client = queryClient

                CoroutineScopeContext.Provider(MainScope()) {
                    app()
                }
            }
        }
    )
}
