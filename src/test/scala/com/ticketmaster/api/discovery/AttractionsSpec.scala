package com.ticketmaster.api.discovery

import java.time.ZonedDateTime

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

class AttractionsSpec extends ApiSpec with HttpDsl {

  override implicit val patienceConfig = PatienceConfig(2 seconds, 200 millis)

  val apiKey = "12345"

  val responseHeaders = Map("Rate-Limit" -> "5000",
    "Rate-Limit-Available" -> "5000",
    "Rate-Limit-Over" -> "0",
    "Rate-Limit-Reset" -> "1453180594367")

  behavior of "discovery attraction API"

  it should "search for an attraction by keyword" in {
    val expectedUrl = s"https://app.ticketmaster.com/discovery/v2/attractions.json?keyword=coachella&apikey=${apiKey}"
    val request = requestMatcher(expectedUrl)

    val response = mockResponse withStatus 200 withHeaders responseHeaders withBody AttractionsSpec.searchAttractionsResponse

    val http = mockHttp expects request returns response

    val api = new DefaultDiscoveryApi(apiKey, http)
    val pendingResponse: Future[PageResponse[Attractions]] = api.searchAttractions(SearchAttractionsRequest(keyword = "coachella"))

    whenReady(pendingResponse) { r =>
      r.pageResult.result.attractions.size should be(1)
      r.pageResult.result.attractions.head.name should be("Coachella Valley Music and Arts Festival")
      r.pageResult.page should be(Page(20, 1, 1, 0))
      r.pageResult.links.self should be(Link("/discovery/v2/attractions.json{?page,size,sort}", Some(true)))
      r.rateLimits should be(RateLimits(5000, 5000, 0, ZonedDateTime.parse("2016-01-19T05:16:34.367Z[UTC]")))
    }
  }

  it should "get an attraction" in {
    val expectedUrl = s"https://app.ticketmaster.com/discovery/v2/attractions/K8vZ9171q60.json?apikey=${apiKey}"
    val request = requestMatcher(expectedUrl)

    val response = mockResponse withStatus 200 withHeaders responseHeaders withBody AttractionsSpec.getAttractionResponse

    val http = mockHttp expects request returns response

    val api = new DefaultDiscoveryApi(apiKey, http)
    val pendingResponse: Future[Response[Attraction]] = api.getAttraction(GetAttractionRequest("K8vZ9171q60"))

    whenReady(pendingResponse) { r =>
      r.result.id should be("K8vZ9171q60")
    }
  }

  it should "throw exception if attraction not found" in {
    val response = mockResponse withStatus 404 withBody AttractionsSpec.error404

    val http = mockHttp expects anything returns response

    val api = new DefaultDiscoveryApi(apiKey, http)
    val pendingResponse: Future[Response[Attraction]] = api.getAttraction(GetAttractionRequest("12345"))

    whenReady(pendingResponse.failed) { t =>
      t shouldBe a[ResourceNotFoundException]
      t.getMessage should be("Resource not found with provided criteria (locale=en-us, id=12345)")
    }
  }
}

object AttractionsSpec {
  val searchAttractionsResponse =
    """
      |{
      |	"_links": {
      |		"self": {
      |			"href": "/discovery/v2/attractions.json{?page,size,sort}",
      |			"templated": true
      |		}
      |	},
      |	"_embedded": {
      |		"attractions": [{
      |			"name": "Coachella Valley Music and Arts Festival",
      |			"type": "attraction",
      |			"id": "K8vZ9171q60",
      |			"test": false,
      |			"locale": "en-us",
      |			"images": [{
      |				"ratio": "16_9",
      |				"url": "https://s1.ticketm.net/dbimages/73990a.jpg",
      |				"width": 205,
      |				"height": 115,
      |				"fallback": false
      |			}, {
      |				"ratio": "4_3",
      |				"url": "https://s1.ticketm.net/dbimages/73988a.jpg",
      |				"width": 305,
      |				"height": 225,
      |				"fallback": false
      |			}],
      |			"classifications": [{
      |				"primary": true,
      |				"segment": {
      |					"id": "KZFzniwnSyZfZ7v7nJ",
      |					"name": "Music"
      |				},
      |				"genre": {
      |					"id": "KnvZfZ7vAeA",
      |					"name": "Rock"
      |				},
      |				"subGenre": {
      |					"id": "KZazBEonSMnZfZ7v6dt",
      |					"name": "Alternative Rock"
      |				}
      |			}],
      |			"_links": {
      |				"self": {
      |					"href": "/discovery/v2/attractions/K8vZ9171q60?locale=en-us"
      |				}
      |			}
      |		}]
      |	},
      |	"page": {
      |		"size": 20,
      |		"totalElements": 1,
      |		"totalPages": 1,
      |		"number": 0
      |	}
      |}
    """.stripMargin


  val getAttractionResponse =
    """
      |{
      |	"name": "Coachella Valley Music and Arts Festival",
      |	"type": "attraction",
      |	"id": "K8vZ9171q60",
      |	"test": false,
      |	"locale": "en-us",
      |	"images": [{
      |		"ratio": "16_9",
      |		"url": "https://s1.ticketm.net/dbimages/73990a.jpg",
      |		"width": 205,
      |		"height": 115,
      |		"fallback": false
      |	}, {
      |		"ratio": "4_3",
      |		"url": "https://s1.ticketm.net/dbimages/73988a.jpg",
      |		"width": 305,
      |		"height": 225,
      |		"fallback": false
      |	}],
      |	"classifications": [{
      |		"primary": true,
      |		"segment": {
      |			"id": "KZFzniwnSyZfZ7v7nJ",
      |			"name": "Music"
      |		},
      |		"genre": {
      |			"id": "KnvZfZ7vAeA",
      |			"name": "Rock"
      |		},
      |		"subGenre": {
      |			"id": "KZazBEonSMnZfZ7v6dt",
      |			"name": "Alternative Rock"
      |		}
      |	}],
      |	"_links": {
      |		"self": {
      |			"href": "/discovery/v2/attractions/K8vZ9171q60?locale=en-us"
      |		}
      |	}
      |}
    """.stripMargin

  val error404 =
    """
      |{
      |	"errors": [{
      |		"code": "DIS1004",
      |		"detail": "Resource not found with provided criteria (locale=en-us, id=12345)",
      |		"status": "404",
      |		"_links": {
      |			"about": {
      |				"href": "/discovery/v2/errors.html#DIS1004"
      |			}
      |		}
      |	}]
      |}
    """.stripMargin
}