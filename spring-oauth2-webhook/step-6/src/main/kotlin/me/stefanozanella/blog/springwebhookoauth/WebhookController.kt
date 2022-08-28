package me.stefanozanella.blog.springwebhookoauth

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@RestController
class WebhookController {
  @Autowired
  private lateinit var client: WebClient

  @Autowired
  private lateinit var authorizedClientService: OAuth2AuthorizedClientService

  @PostMapping("/webhooks/hubspot")
  fun hubspot(@RequestBody events: List<Map<String, String>>) {
    events
      .map { event ->
        println("########################")
        println(authorizedClientService.loadAuthorizedClient("hubspot", event["portalId"]))
        println("########################")

        client
          .get()
          .uri("https://api.hubapi.com/crm/v3/objects/deals/{objectId}") { uri ->
            uri
              .queryParam("properties", "hubspot_owner_id,hs_arr,amount,dealstage")
              .build(event["objectId"])
          }
          .retrieve()
          .bodyToMono<String>()
          .block()
      }
      .forEach(::println)
  }
}
