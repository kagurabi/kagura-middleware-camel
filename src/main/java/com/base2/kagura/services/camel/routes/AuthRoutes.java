/*
   Copyright 2014 base2Services

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.base2.kagura.services.camel.routes;

import com.base2.kagura.rest.exceptions.AuthenticationException;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;

/**
 * @author aubels
 *         Date: 26/08/13
 */
public class AuthRoutes extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        from("cxfrs:bean:rsAuthServer?bindingStyle=SimpleConsumer")
                .log("Executed ${header.operationName}")
                .doTry()
                    .recipientList(simple("direct:rs-${header.operationName}")).end()
                .doCatch(AuthenticationException.class)
                    .beanRef("authBean", "buildAuthFail")
                    .marshal().json(JsonLibrary.Jackson)
                .end()
                .routeId("cxfrsAuthInRouteId");

        from("direct:rs-getAuthToken")
                .beanRef("authBean", "authenticate")
                .marshal().json(JsonLibrary.Jackson)
                .routeId("rsGetAuthRouteId");

        from("direct:rs-testAuthToken")
                .doTry()
                    .beanRef("authBean", "isLoggedIn")
                    .setBody(constant("OK"))
                .doCatch(AuthenticationException.class)
                    .setBody(constant("Not OK"))
                .end()
                .routeId("rsTestAuthTokenRouteId");

        from("direct:rs-logout")
                .doTry()
                    .beanRef("authBean", "logout")
                    .setBody(constant("Done"))
                .doCatch(AuthenticationException.class)
                    .setBody(constant("Not done"))
                .end()
                .routeId("rsLogoutRouteId");

        from("direct:rs-getReports")
                .beanRef("authBean", "getReports")
                .marshal().json(JsonLibrary.Jackson)
                .routeId("rsGetReportsRouteId");

        from("direct:rs-getReportsDetailed")
                .beanRef("authBean", "getReportsDetailed")
                .marshal().json(JsonLibrary.Jackson)
                .routeId("rsGetReportsDetailedRouteId");

        from("timer:cleanAuths?period=600000")
                .log("cleaning Auth routes")
                .beanRef("authBean", "cleanAuthTickets")
                .routeId("cleanAuthsRouteId");
    }
}
