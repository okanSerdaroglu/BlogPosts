package com.example.blogposts.ui.main.blog.state

import com.example.blogposts.models.BlogPost

data class BlogViewState(

     // BlogFragment vars
    var blogFields : BlogFields = BlogFields()
    // ViewBlogFragment vars

    // UpdateBlogFragment vars

) {

    data class BlogFields(
        var blogList: List<BlogPost> = ArrayList<BlogPost>(),
        var searchQuery: String = ""
    )

}