/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.jms;

import javax.jms.ConnectionFactory;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

import static org.apache.camel.component.jms.JmsComponent.jmsComponentAutoAcknowledge;

/**
 * JMS with XPath
 */
public class JmsXPathHeaderTest extends CamelTestSupport {

    @Test
    public void testTrue() throws Exception {
        getMockEndpoint("mock:true").expectedMessageCount(1);
        getMockEndpoint("mock:other").expectedMessageCount(0);

        template.sendBodyAndHeader("activemq:queue:in", "<hello>World</hello>", "foo", "true");

        assertMockEndpointsSatisfied();
    }

    @Test
    public void testFalse() throws Exception {
        getMockEndpoint("mock:true").expectedMessageCount(0);
        getMockEndpoint("mock:other").expectedMessageCount(1);

        template.sendBodyAndHeader("activemq:queue:in", "<hello>World</hello>", "foo", "false");

        assertMockEndpointsSatisfied();
    }

    @Test
    public void testNoHeader() throws Exception {
        getMockEndpoint("mock:true").expectedMessageCount(0);
        getMockEndpoint("mock:other").expectedMessageCount(1);

        template.sendBody("activemq:queue:in", "<hello>World</hello>");

        assertMockEndpointsSatisfied();
    }

    protected CamelContext createCamelContext() throws Exception {
        CamelContext camelContext = super.createCamelContext();

        ConnectionFactory connectionFactory = CamelJmsTestHelper.createConnectionFactory();
        camelContext.addComponent("activemq", jmsComponentAutoAcknowledge(connectionFactory));

        return camelContext;
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("activemq:queue:in")
                    .choice()
                        .when().xpath("$foo = 'true'")
                            .to("activemq:queue:true")
                        .otherwise()
                            .to("activemq:queue:other")
                        .end();

                from("activemq:queue:true").to("mock:true");
                from("activemq:queue:other").to("mock:other");
            }
        };
    }

}