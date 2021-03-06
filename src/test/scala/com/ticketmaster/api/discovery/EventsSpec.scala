package com.ticketmaster.api.discovery

import java.time.{ZoneId, Instant, Clock, ZonedDateTime}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

class EventsSpec extends ApiSpec with HttpDsl {

  override implicit val patienceConfig = PatienceConfig(2 seconds, 200 millis)

  val apiKey = "12345"

  val responseHeaders = Map("Rate-Limit" -> "5000",
    "Rate-Limit-Available" -> "5000",
    "Rate-Limit-Over" -> "0",
    "Rate-Limit-Reset" -> "1453180594367")

  behavior of "discovery event API"

  it should "search for an event by keyword" in {
    val expectedUrl = s"https://app.ticketmaster.com/discovery/v2/events.json?keyword=coachella&apikey=${apiKey}"
    val request = requestMatcher(expectedUrl)

    val response = mockResponse withStatus 200 withHeaders responseHeaders withBody EventsSpec.searchEventsResponse

    val http = mockHttp expects request returns response

    val api = new DefaultDiscoveryApi(apiKey, http)
    val pendingResponse: Future[PageResponse[Events]] = api.searchEvents(SearchEventsRequest(keyword = "coachella"))

    whenReady(pendingResponse) { r =>
      r.pageResult.result.events.size should be(1)
      r.pageResult.result.events.head.name should be("The Fearless Freakcast Presents: Live From Coachella Music Festival")
      r.pageResult.page should be(Page(20, 1, 1, 0))
      r.pageResult.links.self should be(Link("/discovery/v2/events.json{?page,size,sort}", Some(true)))
      r.rateLimits should be(RateLimits(5000, 5000, 0, ZonedDateTime.parse("2016-01-19T05:16:34.367Z[UTC]")))
    }
  }

  it should "search for an event by start date" in {
    val expectedUrl = s"https://app.ticketmaster.com/discovery/v2/events.json?startDateTime=2016-04-20T02:00:00Z&apikey=${apiKey}"
    val request = requestMatcher(expectedUrl)

    val response = mockResponse withStatus 200 withHeaders responseHeaders withBody EventsSpec.searchEventsResponse

    val http = mockHttp expects request returns response

    val api = new DefaultDiscoveryApi(apiKey, http)
    val searchEventsRequest = SearchEventsRequest(startDateTime = ZonedDateTime.now(Clock.fixed(Instant.ofEpochMilli(1461117600000L), ZoneId.of("UTC"))))
    val pendingResponse: Future[PageResponse[Events]] = api.searchEvents(searchEventsRequest)

    whenReady(pendingResponse) { r =>
      r.pageResult.result.events.size should be(1)
      r.pageResult.result.events.head.name should be("The Fearless Freakcast Presents: Live From Coachella Music Festival")
      r.pageResult.page should be(Page(20, 1, 1, 0))
      r.pageResult.links.self should be(Link("/discovery/v2/events.json{?page,size,sort}", Some(true)))
      r.rateLimits should be(RateLimits(5000, 5000, 0, ZonedDateTime.parse("2016-01-19T05:16:34.367Z[UTC]")))
    }
  }

  it should "get event images" in {
    val expectedUrl = s"https://app.ticketmaster.com/discovery/v2/events/k7vGFfdS_Gp6G/images.json?apikey=${apiKey}"
    val request = requestMatcher(expectedUrl)

    val response = mockResponse withStatus 200 withHeaders responseHeaders withBody EventsSpec.getEventImagesResponse

    val http = mockHttp expects request returns response

    val api = new DefaultDiscoveryApi(apiKey, http)
    val pendingResponse: Future[Response[EventImages]] = api.getEventImages(GetEventImagesRequest("k7vGFfdS_Gp6G"))

    whenReady(pendingResponse) { r =>
      r.result.imageType should be("event")
      r.result.id should be("k7vGFfdS_Gp6G")
      r.result.images should have length(10)
    }
  }

  it should "get an event" in {
    val expectedUrl = s"https://app.ticketmaster.com/discovery/v2/events/1AtZAvvGkdzqJ-n.json?apikey=${apiKey}"
    val request = requestMatcher(expectedUrl)

    val response = mockResponse withStatus 200 withHeaders responseHeaders withBody EventsSpec.getEventResponse

    val http = mockHttp expects request returns response

    val api = new DefaultDiscoveryApi(apiKey, http)
    val pendingResponse: Future[Response[Event]] = api.getEvent(GetEventRequest("1AtZAvvGkdzqJ-n"))

    whenReady(pendingResponse) { r =>
      r.result.id should be("1AtZAvvGkdzqJ-n")
    }
  }

  it should "throw exception if event not found" in {
    val response = mockResponse withStatus 404 withBody EventsSpec.error404

    val http = mockHttp expects anything returns response

    val api = new DefaultDiscoveryApi(apiKey, http)
    val pendingResponse: Future[Response[Event]] = api.getEvent(GetEventRequest("12345"))

    whenReady(pendingResponse.failed) { t =>
      t shouldBe a[ResourceNotFoundException]
      t.getMessage should be("Resource not found with provided criteria (locale=en-us, id=12345)")
    }
  }
}

object EventsSpec {
  val searchEventsResponse =
    """
      |{
      |	"_links": {
      |		"self": {
      |			"href": "/discovery/v2/events.json{?page,size,sort}",
      |			"templated": true
      |		}
      |	},
      |	"_embedded": {
      |		"events": [{
      |			"name": "The Fearless Freakcast Presents: Live From Coachella Music Festival",
      |			"type": "event",
      |			"id": "16e0Zf6jYG7c35T",
      |			"test": false,
      |			"url": "http://www.ticketweb.com/t3/sale/SaleEventDetail?dispatch=loadSelectionData&eventId=6582835&REFERRAL_ID=tmfeed",
      |			"locale": "en-us",
      |			"images": [{
      |				"ratio": "16_9",
      |				"url": "http://s1.ticketm.net/dam/c/ARTS_TABLET_LANDSCAPE_16_9.jpg",
      |				"width": 1024,
      |				"height": 576,
      |				"fallback": true
      |			}, {
      |				"ratio": "3_2",
      |				"url": "http://s1.ticketm.net/dam/c/ARTS_TABLET_LANDSCAPE_3_2.jpg",
      |				"width": 1024,
      |				"height": 683,
      |				"fallback": true
      |			}, {
      |				"ratio": "16_9",
      |				"url": "http://s1.ticketm.net/dam/c/ARTS_TABLET_LANDSCAPE_LARGE_16_9.jpg",
      |				"width": 2048,
      |				"height": 1152,
      |				"fallback": true
      |			}, {
      |				"ratio": "16_9",
      |				"url": "http://s1.ticketm.net/dam/c/ARTS_RECOMENDATION_16_9.jpg",
      |				"width": 100,
      |				"height": 56,
      |				"fallback": true
      |			}, {
      |				"ratio": "4_3",
      |				"url": "http://s1.ticketm.net/dam/c/ARTS_CUSTOM.jpg",
      |				"width": 305,
      |				"height": 225,
      |				"fallback": true
      |			}, {
      |				"ratio": "3_2",
      |				"url": "http://s1.ticketm.net/dam/c/ARTS_ARTIST_PAGE_3_2.jpg",
      |				"width": 305,
      |				"height": 203,
      |				"fallback": true
      |			}, {
      |				"ratio": "16_9",
      |				"url": "http://s1.ticketm.net/dam/c/ARTS_RETINA_PORTRAIT_16_9.jpg",
      |				"width": 640,
      |				"height": 360,
      |				"fallback": true
      |			}, {
      |				"ratio": "3_2",
      |				"url": "http://s1.ticketm.net/dam/c/ARTS_RETINA_PORTRAIT_3_2.jpg",
      |				"width": 640,
      |				"height": 427,
      |				"fallback": true
      |			}, {
      |				"ratio": "16_9",
      |				"url": "http://s1.ticketm.net/dam/c/ARTS_EVENT_DETAIL_PAGE_16_9.jpg",
      |				"width": 205,
      |				"height": 115,
      |				"fallback": true
      |			}, {
      |				"ratio": "16_9",
      |				"url": "http://s1.ticketm.net/dam/c/ARTS_RETINA_LANDSCAPE_16_9.jpg",
      |				"width": 1136,
      |				"height": 639,
      |				"fallback": true
      |			}],
      |			"sales": {
      |				"public": {
      |					"startDateTime": "2016-03-23T21:15:01Z",
      |					"startTBD": false,
      |					"endDateTime": "2016-04-20T03:00:00Z"
      |				}
      |			},
      |			"dates": {
      |				"start": {
      |					"localDate": "2016-04-19",
      |					"localTime": "22:00:00",
      |					"dateTime": "2016-04-20T03:00:00Z",
      |					"dateTBD": false,
      |					"dateTBA": false,
      |					"timeTBA": false,
      |					"noSpecificTime": false
      |				},
      |				"timezone": "America/Chicago",
      |				"status": {
      |					"code": "onsale"
      |				}
      |			},
      |			"classifications": [{
      |				"primary": true,
      |				"segment": {
      |					"id": "KZFzniwnSyZfZ7v7na",
      |					"name": "Arts & Theatre"
      |				},
      |				"genre": {
      |					"id": "KnvZfZ7vAe1",
      |					"name": "Comedy"
      |				},
      |				"subGenre": {
      |					"id": "KZazBEonSMnZfZ7vF17",
      |					"name": "Comedy"
      |				}
      |			}],
      |			"promoter": {
      |				"id": "0"
      |			},
      |			"_links": {
      |				"self": {
      |					"href": "/discovery/v2/events/16e0Zf6jYG7c35T?locale=en-us"
      |				},
      |				"venues": [{
      |					"href": "/discovery/v2/venues/KovZpZAJaavA?locale=en-us"
      |				}]
      |			},
      |			"_embedded": {
      |				"venues": [{
      |					"name": "iO Theater ",
      |					"type": "venue",
      |					"id": "KovZpZAJaavA",
      |					"test": false,
      |					"locale": "en-us",
      |					"postalCode": "60642",
      |					"timezone": "America/Chicago",
      |					"city": {
      |						"name": "Chicago"
      |					},
      |					"state": {
      |						"name": "Illinois",
      |						"stateCode": "IL"
      |					},
      |					"country": {
      |						"name": "United States Of America",
      |						"countryCode": "US"
      |					},
      |					"address": {
      |						"line1": "1501 N Kingsbury"
      |					},
      |					"location": {
      |						"longitude": "-87.65189968",
      |						"latitude": "41.90830149"
      |					},
      |					"markets": [{
      |						"id": "3"
      |					}],
      |					"dmas": [{
      |						"id": 249
      |					}, {
      |						"id": 373
      |					}],
      |					"_links": {
      |						"self": {
      |							"href": "/discovery/v2/venues/KovZpZAJaavA?locale=en-us"
      |						}
      |					}
      |				}]
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


  val getEventImagesResponse =
    """
      |{
      |	"type": "event",
      |	"id": "k7vGFfdS_Gp6G",
      |	"images": [{
      |		"ratio": "16_9",
      |		"url": "http://s1.ticketm.net/dam/c/MISC_RECOMENDATION_16_9.jpg",
      |		"width": 100,
      |		"height": 56,
      |		"fallback": true
      |	}, {
      |		"ratio": "16_9",
      |		"url": "http://s1.ticketm.net/dam/c/MISC_RETINA_PORTRAIT_16_9.jpg",
      |		"width": 640,
      |		"height": 360,
      |		"fallback": true
      |	}, {
      |		"ratio": "3_2",
      |		"url": "http://s1.ticketm.net/dam/c/MISC_RETINA_PORTRAIT_3_2.jpg",
      |		"width": 640,
      |		"height": 427,
      |		"fallback": true
      |	}, {
      |		"ratio": "3_2",
      |		"url": "http://s1.ticketm.net/dam/c/MISC_ARTIST_PAGE_3_2.jpg",
      |		"width": 305,
      |		"height": 203,
      |		"fallback": true
      |	}, {
      |		"ratio": "16_9",
      |		"url": "http://s1.ticketm.net/dam/c/MISC_TABLET_LANDSCAPE_16_9.jpg",
      |		"width": 1024,
      |		"height": 576,
      |		"fallback": true
      |	}, {
      |		"ratio": "16_9",
      |		"url": "http://s1.ticketm.net/dam/c/MISC_RETINA_LANDSCAPE_16_9.jpg",
      |		"width": 1136,
      |		"height": 639,
      |		"fallback": true
      |	}, {
      |		"ratio": "4_3",
      |		"url": "http://s1.ticketm.net/dam/c/MISC_CUSTOM.jpg",
      |		"width": 305,
      |		"height": 225,
      |		"fallback": true
      |	}, {
      |		"ratio": "16_9",
      |		"url": "http://s1.ticketm.net/dam/c/MISC_TABLET_LANDSCAPE_LARGE_16_9.jpg",
      |		"width": 2048,
      |		"height": 1152,
      |		"fallback": true
      |	}, {
      |		"ratio": "16_9",
      |		"url": "http://s1.ticketm.net/dbimages/234299a.png",
      |		"width": 205,
      |		"height": 115,
      |		"fallback": false
      |	}, {
      |		"ratio": "3_2",
      |		"url": "http://s1.ticketm.net/dam/c/MISC_TABLET_LANDSCAPE_3_2.jpg",
      |		"width": 1024,
      |		"height": 683,
      |		"fallback": true
      |	}],
      |	"_links": {
      |		"self": {
      |			"href": "/discovery/v2/events/k7vGFfdS_Gp6G/images?locale=en-us"
      |		}
      |	}
      |}
    """.stripMargin

  val getEventResponse =
    """
      |{
      |	"name": "Kroq Locals Only Presents: Black Crystal Wolf Kids Coachella Tribute S",
      |	"type": "event",
      |	"id": "1AtZAvvGkdzqJ-n",
      |	"test": false,
      |	"url": "http://www.ticketweb.com/t3/sale/SaleEventDetail?dispatch=loadSelectionData&eventId=6483315&REFERRAL_ID=tmfeed",
      |	"locale": "en-us",
      |	"images": [{
      |		"ratio": "16_9",
      |		"url": "http://s1.ticketm.net/dam/c/MUSIC_RETINA_PORTRAIT_16_9.jpg",
      |		"width": 640,
      |		"height": 360,
      |		"fallback": true
      |	}, {
      |		"ratio": "16_9",
      |		"url": "http://s1.ticketm.net/dam/c/MUSIC_EVENT_DETAIL_PAGE_16_9.jpg",
      |		"width": 205,
      |		"height": 115,
      |		"fallback": true
      |	}, {
      |		"ratio": "3_2",
      |		"url": "http://s1.ticketm.net/dam/c/MUSIC_ARTIST_PAGE_3_2.jpg",
      |		"width": 305,
      |		"height": 203,
      |		"fallback": true
      |	}, {
      |		"ratio": "16_9",
      |		"url": "http://s1.ticketm.net/dam/c/MUSIC_RETINA_LANDSCAPE_16_9.jpg",
      |		"width": 1136,
      |		"height": 639,
      |		"fallback": true
      |	}, {
      |		"ratio": "16_9",
      |		"url": "http://s1.ticketm.net/dam/c/MUSIC_TABLET_LANDSCAPE_16_9.jpg",
      |		"width": 1024,
      |		"height": 576,
      |		"fallback": true
      |	}, {
      |		"ratio": "3_2",
      |		"url": "http://s1.ticketm.net/dam/c/MUSIC_TABLET_LANDSCAPE_3_2.jpg",
      |		"width": 1024,
      |		"height": 683,
      |		"fallback": true
      |	}, {
      |		"ratio": "16_9",
      |		"url": "http://s1.ticketm.net/dam/c/MUSIC_TABLET_LANDSCAPE_LARGE_16_9.jpg",
      |		"width": 2048,
      |		"height": 1152,
      |		"fallback": true
      |	}, {
      |		"ratio": "4_3",
      |		"url": "http://s1.ticketm.net/dam/c/MUSIC_CUSTOM.jpg",
      |		"width": 305,
      |		"height": 225,
      |		"fallback": true
      |	}, {
      |		"ratio": "3_2",
      |		"url": "http://s1.ticketm.net/dam/c/MUSIC_RETINA_PORTRAIT_3_2.jpg",
      |		"width": 640,
      |		"height": 427,
      |		"fallback": true
      |	}, {
      |		"ratio": "16_9",
      |		"url": "http://s1.ticketm.net/dam/c/MUSIC_RECOMENDATION_16_9.jpg",
      |		"width": 100,
      |		"height": 56,
      |		"fallback": true
      |	}],
      |	"sales": {
      |		"public": {
      |			"startDateTime": "2016-02-01T17:35:01Z",
      |			"startTBD": false,
      |			"endDateTime": "2016-04-11T03:00:00Z"
      |		}
      |	},
      |	"dates": {
      |		"start": {
      |			"localDate": "2016-04-10",
      |			"localTime": "20:00:00",
      |			"dateTime": "2016-04-11T03:00:00Z",
      |			"dateTBD": false,
      |			"dateTBA": false,
      |			"timeTBA": false,
      |			"noSpecificTime": false
      |		},
      |		"timezone": "America/Los_Angeles",
      |		"status": {
      |			"code": "onsale"
      |		}
      |	},
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
      |			"id": "KZazBEonSMnZfZ7v6F1",
      |			"name": "Pop"
      |		}
      |	}],
      |	"promoter": {
      |		"id": "0"
      |	},
      |	"_links": {
      |		"self": {
      |			"href": "/discovery/v2/events/1AtZAvvGkdzqJ-n?locale=en-us"
      |		},
      |		"venues": [{
      |			"href": "/discovery/v2/venues/KovZpZAFJ71A?locale=en-us"
      |		}]
      |	},
      |	"_embedded": {
      |		"venues": [{
      |			"name": "Molly Malones",
      |			"type": "venue",
      |			"id": "KovZpZAFJ71A",
      |			"test": false,
      |			"locale": "en-us",
      |			"postalCode": "90036",
      |			"timezone": "America/Los_Angeles",
      |			"city": {
      |				"name": "Los Angeles"
      |			},
      |			"state": {
      |				"name": "California",
      |				"stateCode": "CA"
      |			},
      |			"country": {
      |				"name": "United States Of America",
      |				"countryCode": "US"
      |			},
      |			"address": {
      |				"line1": "575 South Fairfax Avenue"
      |			},
      |			"location": {
      |				"longitude": "-118.36155660",
      |				"latitude": "34.06529230"
      |			},
      |			"markets": [{
      |				"id": "27"
      |			}],
      |			"dmas": [{
      |				"id": 223
      |			}, {
      |				"id": 324
      |			}, {
      |				"id": 354
      |			}, {
      |				"id": 383
      |			}],
      |			"_links": {
      |				"self": {
      |					"href": "/discovery/v2/venues/KovZpZAFJ71A?locale=en-us"
      |				}
      |			}
      |		}]
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