package com.example.blogposts.ui.main.blog.state

import android.net.Uri
import com.example.blogposts.models.BlogPost
import com.example.blogposts.persistesnce.BlogQueryUtils.Companion.BLOG_ORDER_ASC
import com.example.blogposts.persistesnce.BlogQueryUtils.Companion.ORDER_BY_ASC_DATE_UPDATED

data class BlogViewState(

    // BlogFragment vars
    var blogFields: BlogFields = BlogFields(),

    // ViewBlogFragment vars
    var viewBlogFields: ViewBlogFields = ViewBlogFields(),

    // UpdateBlogFragment vars
    var updateBlogFields: UpdateBlogFields = UpdateBlogFields()

) {

    data class BlogFields(
        var blogList: List<BlogPost> = ArrayList(),
        var searchQuery: String = "",
        var page: Int = 1,
        var isQueryInProgress: Boolean = false,
        var isQueryExhausted: Boolean = false,
        var filter: String = ORDER_BY_ASC_DATE_UPDATED, // date updated
        var order: String = BLOG_ORDER_ASC
    )

    data class ViewBlogFields(
        var blogPost: BlogPost? = null,
        var isAuthorOfBLogPost: Boolean = false
    )

    data class UpdateBlogFields(
        var updatedBlogTitle: String? = null,
        var updatedBlogBody: String? = null,
        var updatedImageUri: Uri? = null
    )


}