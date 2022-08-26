package me.stefanozanella.blog.springwebhookoauth

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain

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
      .and().and()
      .build()
}
