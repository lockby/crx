package com.crstlnz.komikchino.data.model

import com.fasterxml.jackson.annotation.JsonProperty

data class GithubModel(
    @JsonProperty("url") var url: String? = null,
    @JsonProperty("assets_url") var assetsUrl: String? = null,
    @JsonProperty("upload_url") var uploadUrl: String? = null,
    @JsonProperty("html_url") var htmlUrl: String? = null,
    @JsonProperty("id") var id: Int? = null,
    @JsonProperty("author") var author: Author? = Author(),
    @JsonProperty("node_id") var nodeId: String? = null,
    @JsonProperty("tag_name") var tagName: String? = null,
    @JsonProperty("target_commitish") var targetCommitish: String? = null,
    @JsonProperty("name") var name: String? = null,
    @JsonProperty("draft") var draft: Boolean? = null,
    @JsonProperty("prerelease") var prerelease: Boolean? = null,
    @JsonProperty("created_at") var createdAt: String? = null,
    @JsonProperty("published_at") var publishedAt: String? = null,
    @JsonProperty("assets") var assets: ArrayList<Assets> = arrayListOf(),
    @JsonProperty("tarball_url") var tarballUrl: String? = null,
    @JsonProperty("zipball_url") var zipballUrl: String? = null,
    @JsonProperty("body") var body: String? = null
)


data class Author(
    @JsonProperty("login") var login: String? = null,
    @JsonProperty("id") var id: Int? = null,
    @JsonProperty("node_id") var nodeId: String? = null,
    @JsonProperty("avatar_url") var avatarUrl: String? = null,
    @JsonProperty("gravatar_id") var gravatarId: String? = null,
    @JsonProperty("url") var url: String? = null,
    @JsonProperty("html_url") var htmlUrl: String? = null,
    @JsonProperty("followers_url") var followersUrl: String? = null,
    @JsonProperty("following_url") var followingUrl: String? = null,
    @JsonProperty("gists_url") var gistsUrl: String? = null,
    @JsonProperty("starred_url") var starredUrl: String? = null,
    @JsonProperty("subscriptions_url") var subscriptionsUrl: String? = null,
    @JsonProperty("organizations_url") var organizationsUrl: String? = null,
    @JsonProperty("repos_url") var reposUrl: String? = null,
    @JsonProperty("events_url") var eventsUrl: String? = null,
    @JsonProperty("received_events_url") var receivedEventsUrl: String? = null,
    @JsonProperty("type") var type: String? = null,
    @JsonProperty("site_admin") var siteAdmin: Boolean? = null
)

data class Uploader(
    @JsonProperty("login") var login: String? = null,
    @JsonProperty("id") var id: Int? = null,
    @JsonProperty("node_id") var nodeId: String? = null,
    @JsonProperty("avatar_url") var avatarUrl: String? = null,
    @JsonProperty("gravatar_id") var gravatarId: String? = null,
    @JsonProperty("url") var url: String? = null,
    @JsonProperty("html_url") var htmlUrl: String? = null,
    @JsonProperty("followers_url") var followersUrl: String? = null,
    @JsonProperty("following_url") var followingUrl: String? = null,
    @JsonProperty("gists_url") var gistsUrl: String? = null,
    @JsonProperty("starred_url") var starredUrl: String? = null,
    @JsonProperty("subscriptions_url") var subscriptionsUrl: String? = null,
    @JsonProperty("organizations_url") var organizationsUrl: String? = null,
    @JsonProperty("repos_url") var reposUrl: String? = null,
    @JsonProperty("events_url") var eventsUrl: String? = null,
    @JsonProperty("received_events_url") var receivedEventsUrl: String? = null,
    @JsonProperty("type") var type: String? = null,
    @JsonProperty("site_admin") var siteAdmin: Boolean? = null
)

data class Assets(
    @JsonProperty("url") var url: String? = null,
    @JsonProperty("id") var id: Int? = null,
    @JsonProperty("node_id") var nodeId: String? = null,
    @JsonProperty("name") var name: String? = null,
    @JsonProperty("label") var label: String? = null,
    @JsonProperty("uploader") var uploader: Uploader? = Uploader(),
    @JsonProperty("content_type") var contentType: String? = null,
    @JsonProperty("state") var state: String? = null,
    @JsonProperty("size") var size: Int? = null,
    @JsonProperty("download_count") var downloadCount: Int? = null,
    @JsonProperty("created_at") var createdAt: String? = null,
    @JsonProperty("updated_at") var updatedAt: String? = null,
    @JsonProperty("browser_download_url") var browserDownloadUrl: String? = null
)