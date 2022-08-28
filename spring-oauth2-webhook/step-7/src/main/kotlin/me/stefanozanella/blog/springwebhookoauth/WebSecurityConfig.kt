package me.stefanozanella.blog.springwebhookoauth

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.RequestEntity
import org.springframework.jdbc.core.JdbcOperations
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.oauth2.client.*
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebSecurityConfig {
  @Bean
  fun oauth2Configuration(http: HttpSecurity): SecurityFilterChain =
    http
      .oauth2Client()
      .and()
      .oauth2Login()
      .redirectionEndpoint()
      .baseUri("/oauth2/callback/*")
      .and()
      .userInfoEndpoint()
      .userService(oauth2UserService())
      .and().and()
      .csrf().disable()
      .build()

  @Bean
  fun authorizedClientManager(
    clientRegistrationRepository: ClientRegistrationRepository,
    authorizedClientService: OAuth2AuthorizedClientService,
  ) =
    AuthorizedClientServiceOAuth2AuthorizedClientManager(
      clientRegistrationRepository,
      authorizedClientService,
    ).apply {
      setAuthorizedClientProvider(
        OAuth2AuthorizedClientProviderBuilder
          .builder()
          .clientCredentials()
          .build(),
      )
    }

  @Bean
  fun webClient(authorizedClientManager: OAuth2AuthorizedClientManager): WebClient =
    WebClient
      .builder()
      .apply(
        ServletOAuth2AuthorizedClientExchangeFilterFunction(
          authorizedClientManager
        ).oauth2Configuration()
      )
      .build()

  @Bean
  fun authorizedClientService(
    jdbcOperations: JdbcOperations,
    clientRegistrationRepository: ClientRegistrationRepository,
  ): OAuth2AuthorizedClientService =
    JdbcOAuth2AuthorizedClientService(jdbcOperations, clientRegistrationRepository)

  private fun oauth2UserService() = DefaultOAuth2UserService().apply {
    setRequestEntityConverter { request ->
      RequestEntity.get(
        request.clientRegistration.providerDetails.userInfoEndpoint.uri,
        request.accessToken.tokenValue,
      ).build()
    }
  }
}
