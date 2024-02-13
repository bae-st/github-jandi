package com.onetatwopi.jandi.layout

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowAnchor
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.platform.PlatformProjectOpenProcessor.Companion.isNewProject
import com.onetatwopi.jandi.data.RepositoryParser
import com.onetatwopi.jandi.data.pullRequest.PullRequestService
import com.onetatwopi.jandi.layout.dto.IssueInfo
import com.onetatwopi.jandi.layout.dto.PullRequestInfo
import com.onetatwopi.jandi.layout.panel.TabbedPanel
import com.onetatwopi.jandi.layout.panel.issue.IssuePanel
import com.onetatwopi.jandi.layout.panel.pullRequest.PullRequestPanel
import kotlinx.serialization.json.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


private val stubbingPullRequestData = listOf(
    PullRequestInfo(
        "Pull Request 1",
        "User1",
        "Open",
        "https://www.naver.com",
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    ),
    PullRequestInfo(
        "Pull Request 2",
        "User2",
        "Closed",
        "https://www.naver.com",
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    ),
    PullRequestInfo(
        "Pull Request 3",
        "User3",
        "In Progress",
        "https://www.naver.com",
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    )
)

const val mockIssue =
    "[{\"url\":\"https://api.github.com/repos/graceful-martin/hanghae99/issues/17\",\"repository_url\":\"https://api.github.com/repos/graceful-martin/hanghae99\",\"labels_url\":\"https://api.github.com/repos/graceful-martin/hanghae99/issues/17/labels{/name}\",\"comments_url\":\"https://api.github.com/repos/graceful-martin/hanghae99/issues/17/comments\",\"events_url\":\"https://api.github.com/repos/graceful-martin/hanghae99/issues/17/events\",\"html_url\":\"https://github.com/graceful-martin/hanghae99/issues/17\",\"id\":2116919822,\"node_id\":\"I_kwDOK1ejls5-LaIO\",\"number\":17,\"title\":\"Test\",\"user\":{\"login\":\"graceful-martin\",\"id\":56020202,\"node_id\":\"MDQ6VXNlcjU2MDIwMjAy\",\"avatar_url\":\"https://avatars.githubusercontent.com/u/56020202?v=4\",\"gravatar_id\":\"\",\"url\":\"https://api.github.com/users/graceful-martin\",\"html_url\":\"https://github.com/graceful-martin\",\"followers_url\":\"https://api.github.com/users/graceful-martin/followers\",\"following_url\":\"https://api.github.com/users/graceful-martin/following{/other_user}\",\"gists_url\":\"https://api.github.com/users/graceful-martin/gists{/gist_id}\",\"starred_url\":\"https://api.github.com/users/graceful-martin/starred{/owner}{/repo}\",\"subscriptions_url\":\"https://api.github.com/users/graceful-martin/subscriptions\",\"organizations_url\":\"https://api.github.com/users/graceful-martin/orgs\",\"repos_url\":\"https://api.github.com/users/graceful-martin/repos\",\"events_url\":\"https://api.github.com/users/graceful-martin/events{/privacy}\",\"received_events_url\":\"https://api.github.com/users/graceful-martin/received_events\",\"type\":\"User\",\"site_admin\":false},\"labels\":[],\"state\":\"open\",\"locked\":false,\"assignee\":null,\"assignees\":[],\"milestone\":null,\"comments\":0,\"created_at\":\"2024-02-04T07:10:47Z\",\"updated_at\":\"2024-02-04T07:10:47Z\",\"closed_at\":null,\"author_association\":\"OWNER\",\"active_lock_reason\":null,\"body\":\"test\",\"reactions\":{\"url\":\"https://api.github.com/repos/graceful-martin/hanghae99/issues/17/reactions\",\"total_count\":0,\"+1\":0,\"-1\":0,\"laugh\":0,\"hooray\":0,\"confused\":0,\"heart\":0,\"rocket\":0,\"eyes\":0},\"timeline_url\":\"https://api.github.com/repos/graceful-martin/hanghae99/issues/17/timeline\",\"performed_via_github_app\":null,\"state_reason\":null}]"

class MainLayout : ToolWindowFactory, DumbAware {

    private var pullRequestService: PullRequestService

    init {
        pullRequestService = PullRequestService()
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        toolWindow.setAnchor(ToolWindowAnchor.LEFT) {
            project.isNewProject()
        }

        val repositoryInfo = RepositoryParser.getOpenedRepository(project)
        println("repositoryInfo = $repositoryInfo")

        // TODO: 공통 처리
        // val (repoOwner, repoName) = RepositoryParser.getOpenedRepository(project)
        // println("repositoryInfo = $repoOwner, $repoName")
        // val modifiedRepoName = repoName.replace(".git", "")

        val tabbedPanel = TabbedPanel()
        //val pullRequestList = pullRequestService.getPullRequestList("shouldAddToken", repositoryInfo)
        val pullRequestPanel = PullRequestPanel
        //pullRequestPanel.setPullRequestInfoList(pullRequestList)
        pullRequestPanel.setPullRequestInfoList(stubbingPullRequestData)
        pullRequestPanel.render()

        val issuePanel = IssuePanel

        val jsonIssues = Json.parseToJsonElement(mockIssue).jsonArray
        val issues = mutableListOf<IssueInfo>()
        for (i in 0 until jsonIssues.size) {
            val jsonIssue = jsonIssues[i].jsonObject
            issues.add(generateIssue(jsonIssue))
        }

        issuePanel.setIssueInfoList(issues)
        issuePanel.render()

        tabbedPanel.addTab(pullRequestPanel)
        tabbedPanel.addTab(issuePanel)

        val contentManager = toolWindow.contentManager
        val content = contentManager.factory.createContent(tabbedPanel.getPanel(), "", false)
        contentManager.addContent(content)
    }

    private fun generateIssue(jsonIssue: JsonObject) = IssueInfo(
        title = jsonIssue["title"]?.jsonPrimitive?.contentOrNull ?: "",
        createUserId = jsonIssue["user"]?.jsonObject?.get("login")?.jsonPrimitive?.contentOrNull ?: "",
        url = jsonIssue["url"]?.jsonPrimitive?.contentOrNull ?: "",
        status = jsonIssue["state"]?.jsonPrimitive?.contentOrNull ?: "",
        openAt = jsonIssue["created_at"]?.jsonPrimitive?.contentOrNull ?: "",
        closeAt = jsonIssue["closed_at"]?.jsonPrimitive?.contentOrNull ?: ""
    )
}