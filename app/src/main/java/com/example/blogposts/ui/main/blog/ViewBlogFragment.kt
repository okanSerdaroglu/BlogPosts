package com.example.blogposts.ui.main.blog

import android.os.Bundle
import android.view.*
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController

import com.example.blogposts.R
import com.example.blogposts.models.BlogPost
import com.example.blogposts.ui.main.blog.state.BlogStateEvent
import com.example.blogposts.ui.main.blog.state.BlogStateEvent.CheckAuthorBlogPostsEvent
import com.example.blogposts.ui.main.blog.viewmodel.isAuthorOfBlogPost
import com.example.blogposts.ui.main.blog.viewmodel.setIsAuthorOfBlogPost
import com.example.blogposts.utils.DateUtil
import kotlinx.android.synthetic.main.fragment_view_blog.*


class ViewBlogFragment : BaseBlogFragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_view_blog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        subscribeObservers()
        stateChangeListener.expandAppbar()
        delete_button.setOnClickListener {
            deleteBlogPost()
        }
    }

    private fun subscribeObservers() {

        viewModel.dataState.observe(viewLifecycleOwner, Observer { dataState ->
            stateChangeListener.onDataStateChange(dataState)
            dataState.data?.let { data ->
                data.data?.getContentIfNotHandled()?.let { viewState ->
                    viewModel.setIsAuthorOfBlogPost(
                        viewState.viewBlogFields.isAuthorOfBLogPost
                    )
                }
            }
        })

        viewModel.viewState.observe(viewLifecycleOwner, Observer { blogViewState ->
            blogViewState.viewBlogFields.blogPost?.let { blogPost ->
                setBlogProperties(blogPost)
            }

            if (blogViewState.viewBlogFields.isAuthorOfBLogPost) {
                adaptViewToAuthorMode()
            }

        })

    }

    private fun adaptViewToAuthorMode() {
        activity?.invalidateOptionsMenu()
        delete_button.visibility = View.VISIBLE
    }

    private fun setBlogProperties(blogPost: BlogPost) {
        requestManager
            .load(blogPost.image)
            .into(blog_image)

        blog_title.text = blogPost.title
        blog_author.text = blogPost.username
        blog_update_date.text = DateUtil.convertLongToStringDate(
            blogPost.date_updated
        )
        blog_body.text = blogPost.body

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (viewModel.isAuthorOfBlogPost()) {
            inflater.inflate(R.menu.edit_view_menu, menu)
        }
    }

    private fun checkAuthorOfBlogPost() {
        viewModel.setIsAuthorOfBlogPost(false) // reset
        viewModel.setStateEvent(CheckAuthorBlogPostsEvent())
    }

    private fun deleteBlogPost() {
        viewModel.setStateEvent(
            BlogStateEvent.DeleteBlogPostEvent()
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (viewModel.isAuthorOfBlogPost()) {
            when (item.itemId) {
                R.id.edit -> {
                    navUpdateBlogFragment()
                    return true
                }
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun navUpdateBlogFragment() {
        findNavController().navigate(R.id.action_viewBlogFragment_to_updateBlogFragment)
    }


}