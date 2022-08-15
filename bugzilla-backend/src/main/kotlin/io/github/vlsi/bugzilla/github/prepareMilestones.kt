package io.github.vlsi.bugzilla.commands

import io.github.vlsi.bugzilla.configuration
import io.github.vlsi.bugzilla.dbexport.Bugs
import io.github.vlsi.bugzilla.dbexport.Milestones
import io.github.vlsi.bugzilla.github.*
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

suspend fun prepareMilestones(
    gitHubParams: GitHubParametersGroup,
    dbParams: DbParametersGroup,
    dryRun: Boolean,
): Map<String, Milestone> {
    val milestones =
        gitHubParams.gitHubApi.listMilestones(
            gitHubParams.organization,
            gitHubParams.issuesRepository,
            state = MilestoneListState.all
        )
            .associateBy { it.title }
            .toMutableMap()
    ImportToGitHub.log.debug("Existing milestones in GitHub: {}", milestones)
    val milestoneRegex = if (!configuration.hasPath("converter-settings.milestone-rename")) {
        null
    } else {
        Regex(configuration.getString("converter-settings.milestone-rename.regex"))
    }
    val milestoneReplacement = if (!configuration.hasPath("converter-settings.milestone-rename")) {
        null
    } else {
        configuration.getString("converter-settings.milestone-rename.replacement")
    }
    val requiredMilestones = newSuspendedTransaction(db = dbParams.connect) {
        (Bugs.join(
            Milestones,
            joinType = JoinType.INNER,
            onColumn = Bugs.target_milestone,
            otherColumn = Milestones.value
        )).slice(Milestones.value, Milestones.isactive, Milestones.sortKey)
            .select { Milestones.value neq "---" }
            .withDistinct()
            .orderBy(Milestones.sortKey)
            .associate {
                it[Milestones.value] to if (it[Milestones.isactive]) MilestoneState.open else MilestoneState.closed
            }
    }
    var nextMilestoneId = milestones.map { it.value.number.value }.maxOrNull() ?: 0
    val milestoneMap = requiredMilestones
        .mapValues {
            var title = it.key
            val state = it.value
            if (milestoneRegex != null && milestoneReplacement != null) {
                title = title.replace(milestoneRegex, milestoneReplacement)
            }
            milestones[title] ?: if (dryRun) {
                ImportToGitHub.log.info("Dry run: skipping create milestone $title, state $state")
                nextMilestoneId += 1
                Milestone(
                    number = MilestoneNumber(nextMilestoneId),
                    title = title
                )
            } else {
                gitHubParams.gitHubApi.createMilestone(
                    gitHubParams.organization,
                    gitHubParams.issuesRepository,
                    MilestoneCreateRequest(
                        title = title,
                        state = state
                    )
                )
            }.also { milestones[title] = it }
        }
    ImportToGitHub.log.debug("All milestones, including created ones: {}", milestoneMap)
    return milestoneMap
}
