package com.example.blogposts.ui.main.blog

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.net.toUri
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController

import com.example.blogposts.R
import com.example.blogposts.models.BlogPost
import com.example.blogposts.ui.AreYouSureCallback
import com.example.blogposts.ui.UIMessage
import com.example.blogposts.ui.UIMessageType
import com.example.blogposts.ui.main.blog.state.BlogStateEvent
import com.example.blogposts.ui.main.blog.state.BlogStateEvent.CheckAuthorBlogPostsEvent
import com.example.blogposts.ui.main.blog.viewmodel.*
import com.example.blogposts.utils.DateUtil
import com.example.blogposts.utils.SuccessHandling.Companion.SUCCESS_BLOG_DELETED
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
        checkAuthorOfBlogPost()
        stateChangeListener.expandAppbar()
        delete_button.setOnClickListener {
            confirmDeleteRequest()
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
                data.response?.peekContent()?.let { response ->
                    if (response.message == SUCCESS_BLOG_DELETED) {
                        viewModel.removeDeletedBlogPost()
                        findNavController().popBackStack()
                    }
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

    private fun confirmDeleteRequest() {
        val callback: AreYouSureCallback = object : AreYouSureCallback {
            override fun proceed() {
                deleteBlogPost()
            }

            override fun cancel() {}

        }
        uiCommunicationListener.onUIMessageReceived(
            UIMessage(
                getString(R.string.are_you_sure_delete),
                UIMessageType.AreYouSureDialog(callback)
            )
        )
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
        try {
            viewModel.setUpdatedBlogFields(
                title = viewModel.getBlogPost().title,
                body = viewModel.getBlogPost().body,
                uri = viewModel.getBlogPost().image.toUri()
            )
            findNavController().navigate(R.id.action_viewBlogFragment_to_updateBlogFragment)
        } catch (e: Exception) {
            Log.e(TAG, "Exception:${e.message}")
        }
    }


}