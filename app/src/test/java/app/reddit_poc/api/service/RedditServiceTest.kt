package app.reddit_poc.api.service

import app.reddit_poc.api.response.post.CommentResponse
import app.reddit_poc.api.response.topic.TopicResponse
import app.reddit_poc.util.asJson
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import java.net.HttpURLConnection


class RedditServiceTest {

    private val moshi: Moshi = Moshi.Builder().build()

    //That`s could be better
    @Test
    fun shouldReturnNullWhenApiReturnsNothing() {
        runBlocking {
            val responseData = "/fake-response/post-empty-list.json".asJson()

            val response = MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(responseData)
            mockWebServer.enqueue(response)


            val postListing = apiService.getTopicData(after = null)


            assertThat(postListing.data).isNull()
        }
    }

   /* @Test
    fun shouldReturnOnly3PostWhenLimitFieldIs3() {
        runBlocking {
            //Given
            //I Want the real behavior from API
            val privateMockWebServer = MockWebServer()
            privateMockWebServer.start()
            privateMockWebServer.url(API_URL)

            //When
            val postListing = apiService.getTopicData(limit = 3, after = null)

            //Assert
            assertThat(postListing.data?.children?.size).isEqualTo(3)
        }
    }*/

    @Test
    fun shouldReturnTopicResponseWhenApiReturnsData() {
        runBlocking {
            val responseData = "/fake-response/post-list.json".asJson()

            val response = MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(responseData)
            mockWebServer.enqueue(response)

            val postListingResult = apiService.getTopicData(after = null)

            val adapter: JsonAdapter<TopicResponse> = moshi.adapter(TopicResponse::class.java)
            val expected = adapter.fromJson(responseData)

            assertThat(postListingResult).isEqualTo(expected)
        }
    }

    @Test
    fun shouldReturnEmptyListOfCommentsWhenPostHasNoComments() {
        runBlocking {
            val responseData = "/fake-response/post-with-no-comments.json".asJson()

            val response = MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(responseData)
            mockWebServer.enqueue(response)

            val postResult = apiService.getSinglePostData(postUrl = POST_WITH_NO_COMMENT_URL, raw_json = 1)

            assertThat(postResult.last().data).isEqualTo(null)
        }
    }

    @Test
    fun shouldReturnPostWithWhenPostHasComments() {
        runBlocking {
            val responseData = "/fake-response/post-with-comments.json".asJson()

            val response = MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(responseData)
            mockWebServer.enqueue(response)

            val postResult = apiService.getSinglePostData(postUrl = POST_WITH_COMMENT_URL, raw_json = 1)

            val type = Types.newParameterizedType(
                List::class.java,
                CommentResponse::class.java
            )
            val adapter: JsonAdapter<List<CommentResponse>> = moshi.adapter(type)
            val expected = adapter.fromJson(responseData)

            assertThat(postResult).isEqualTo(expected)
        }
    }

    companion object {
        const val POST_WITH_NO_COMMENT_URL = "r/brasil/comments/hx2amb/que_podcasts_você_está_ouvindo_24072020/.json"
        const val POST_WITH_COMMENT_URL = "/r/programming/comments/hxq9ut/mezzano_an_os_written_in_common_lisp_has_released/.json"

        private lateinit var mockWebServer: MockWebServer
        private lateinit var apiService: RedditService

        @BeforeClass
        @JvmStatic
        fun setup() {
            mockWebServer = FakeWebServiceFactory.mockWebServer
            apiService = FakeWebServiceFactory.webService
        }

        @AfterClass
        @JvmStatic
        fun teardown() {
            mockWebServer.shutdown()
        }
    }


}