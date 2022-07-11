package io.github.vlsi.bugzilla.ui

import react.VFC
import react.create
import react.dom.html.ReactHTML.div
import react.router.Outlet
import react.router.Route
import react.router.Routes
import react.router.dom.HashRouter
import react.router.dom.Link

val App by VFC {
    div {
        Link {
            to = "/bugs"
            +"Browse bugs"
        }
        +" "
        Link {
            to = "/migrate"
            +"Migrate to GitHub"
        }
    }

    Outlet()
}

val app by VFC {
    HashRouter {
        Routes {
            Route {
                path = "/"
                element = App.create()
                Route {
                    index = true
                    element = SampleBugs.create()
                }
                Route {
                    path = "bugs"
                    element = BugBrowser.create()
                    Route {
                        path = ":bug_id"
                        element = BugInfo.create()
                    }
                }
                Route {
                    path = "migrate"
                    element = Migrate.create()
                }
            }
        }
    }
}
