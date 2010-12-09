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
package org.apache.camel.builder;

import java.util.concurrent.TimeUnit;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.component.seda.SedaEndpoint;

/**
 * @version $Revision$
 */
public class NotifyBuilderTest extends ContextTestSupport {

    public void testDirectWhenExchangeDoneSimple() throws Exception {
        NotifyBuilder event = event()
            .from("direct:foo").whenDone(1)
            .create();

        assertEquals("from(direct:foo).whenDone(1)", event.toString());
        assertEquals(false, event.matches());

        template.sendBody("direct:foo", "A");
        assertEquals(true, event.matches());
    }

    public void testDirectBeerWhenExchangeDoneSimple() throws Exception {
        NotifyBuilder event = event()
            .from("direct:beer").whenDone(1)
            .create();

        assertEquals("from(direct:beer).whenDone(1)", event.toString());
        assertEquals(false, event.matches());

        template.sendBody("direct:beer", "A");
        assertEquals(true, event.matches());
    }

    public void testDirectFromRoute() throws Exception {
        NotifyBuilder event = event()
            .fromRoute("foo").whenDone(1)
            .create();

        assertEquals("fromRoute(foo).whenDone(1)", event.toString());
        assertEquals(false, event.matches());

        template.sendBody("direct:bar", "A");
        assertEquals(false, event.matches());

        template.sendBody("direct:foo", "B");
        assertEquals(true, event.matches());
    }

    public void testDirectFromRouteReceived() throws Exception {
        NotifyBuilder event = event()
            .fromRoute("foo").whenReceived(1)
            .create();

        assertEquals("fromRoute(foo).whenReceived(1)", event.toString());
        assertEquals(false, event.matches());

        template.sendBody("direct:bar", "A");
        assertEquals(false, event.matches());

        template.sendBody("direct:foo", "B");
        assertEquals(true, event.matches());
    }

    public void testWhenExchangeDone() throws Exception {
        NotifyBuilder event = event()
            .from("direct:foo").whenDone(5)
            .create();

        assertEquals("from(direct:foo).whenDone(5)", event.toString());
        assertEquals(false, event.matches());

        template.sendBody("direct:foo", "A");
        template.sendBody("direct:foo", "B");
        template.sendBody("direct:foo", "C");
        template.sendBody("direct:bar", "D");
        template.sendBody("direct:bar", "E");
        assertEquals(false, event.matches());

        template.sendBody("direct:foo", "F");
        template.sendBody("direct:bar", "G");
        assertEquals(false, event.matches());

        template.sendBody("direct:foo", "H");
        template.sendBody("direct:bar", "I");
        assertEquals(true, event.matches());
    }

    public void testWhenExchangeDoneAnd() throws Exception {
        NotifyBuilder event = event()
            .from("direct:foo").whenDone(5)
            .and().from("direct:bar").whenDone(7)
            .create();

        assertEquals(false, event.matches());

        template.sendBody("direct:foo", "A");
        template.sendBody("direct:foo", "B");
        template.sendBody("direct:foo", "C");
        template.sendBody("direct:bar", "D");
        template.sendBody("direct:bar", "E");
        assertEquals(false, event.matches());

        template.sendBody("direct:foo", "F");
        template.sendBody("direct:bar", "G");
        assertEquals(false, event.matches());

        template.sendBody("direct:foo", "H");
        template.sendBody("direct:bar", "I");
        assertEquals(false, event.matches());

        template.sendBody("direct:bar", "J");
        template.sendBody("direct:bar", "K");
        template.sendBody("direct:bar", "L");
        assertEquals(true, event.matches());
    }

    public void testFromRouteWhenExchangeDoneAnd() throws Exception {
        NotifyBuilder event = event()
            .fromRoute("foo").whenDone(5)
            .and().fromRoute("bar").whenDone(7)
            .create();

        assertEquals(false, event.matches());

        template.sendBody("direct:foo", "A");
        template.sendBody("direct:foo", "B");
        template.sendBody("direct:foo", "C");
        template.sendBody("direct:bar", "D");
        template.sendBody("direct:bar", "E");
        assertEquals(false, event.matches());

        template.sendBody("direct:foo", "F");
        template.sendBody("direct:bar", "G");
        assertEquals(false, event.matches());

        template.sendBody("direct:foo", "H");
        template.sendBody("direct:bar", "I");
        assertEquals(false, event.matches());

        template.sendBody("direct:bar", "J");
        template.sendBody("direct:bar", "K");
        template.sendBody("direct:bar", "L");
        assertEquals(true, event.matches());
    }

    public void testFromRouteAndNot() throws Exception {
        NotifyBuilder event = event()
            .fromRoute("foo").whenDone(2)
            .and().fromRoute("bar").whenReceived(1)
            .not().fromRoute("cake").whenDone(1)
            .create();

        assertEquals(false, event.matches());

        template.sendBody("direct:foo", "A");
        template.sendBody("direct:foo", "B");
        assertEquals(false, event.matches());

        template.sendBody("direct:bar", "C");
        assertEquals(true, event.matches());

        template.sendBody("direct:foo", "D");
        template.sendBody("direct:bar", "E");
        assertEquals(true, event.matches());

        // and now the cake to make it false
        template.sendBody("direct:cake", "F");
        assertEquals(false, event.matches());
    }

    public void testWhenExchangeDoneOr() throws Exception {
        NotifyBuilder event = event()
            .from("direct:foo").whenDone(5)
            .or().from("direct:bar").whenDone(7)
            .create();

        assertEquals("from(direct:foo).whenDone(5).or().from(direct:bar).whenDone(7)", event.toString());
        assertEquals(false, event.matches());

        template.sendBody("direct:foo", "A");
        template.sendBody("direct:foo", "B");
        template.sendBody("direct:foo", "C");
        template.sendBody("direct:bar", "D");
        template.sendBody("direct:bar", "E");
        assertEquals(false, event.matches());

        template.sendBody("direct:bar", "G");
        assertEquals(false, event.matches());

        template.sendBody("direct:bar", "I");
        assertEquals(false, event.matches());

        template.sendBody("direct:bar", "J");
        template.sendBody("direct:bar", "K");
        template.sendBody("direct:bar", "L");
        assertEquals(true, event.matches());
    }

    public void testWhenExchangeDoneNot() throws Exception {
        NotifyBuilder event = event()
            .from("direct:foo").whenDone(5)
            .not().from("direct:bar").whenDone(1)
            .create();

        assertEquals("from(direct:foo).whenDone(5).not().from(direct:bar).whenDone(1)", event.toString());
        assertEquals(false, event.matches());

        template.sendBody("direct:foo", "A");
        template.sendBody("direct:foo", "B");
        template.sendBody("direct:foo", "C");
        template.sendBody("direct:foo", "D");
        assertEquals(false, event.matches());

        template.sendBody("direct:foo", "E");
        assertEquals(true, event.matches());

        template.sendBody("direct:foo", "F");
        assertEquals(true, event.matches());

        template.sendBody("direct:bar", "G");
        assertEquals(false, event.matches());
    }

    public void testWhenExchangeDoneOrFailure() throws Exception {
        NotifyBuilder event = event()
            .whenDone(5)
            .or().whenFailed(1)
            .create();

        assertEquals("whenDone(5).or().whenFailed(1)", event.toString());
        assertEquals(false, event.matches());

        template.sendBody("direct:foo", "A");
        template.sendBody("direct:foo", "B");
        template.sendBody("direct:foo", "D");
        assertEquals(false, event.matches());

        try {
            template.sendBody("direct:fail", "E");
        } catch (Exception e) {
            // ignore
        }
        assertEquals(true, event.matches());
    }

    public void testWhenExchangeDoneNotFailure() throws Exception {
        NotifyBuilder event = event()
            .whenDone(5)
            .not().whenFailed(1)
            .create();

        assertEquals(false, event.matches());

        template.sendBody("direct:foo", "A");
        template.sendBody("direct:foo", "B");
        template.sendBody("direct:foo", "D");
        template.sendBody("direct:bar", "E");
        template.sendBody("direct:bar", "F");
        assertEquals(true, event.matches());

        try {
            template.sendBody("direct:fail", "G");
        } catch (Exception e) {
            // ignore
        }
        assertEquals(false, event.matches());
    }

    public void testFilterWhenExchangeDone() throws Exception {
        NotifyBuilder event = event()
            .filter(body().contains("World")).whenDone(3)
            .create();

        assertEquals("filter(body contains World).whenDone(3)", event.toString());
        assertEquals(false, event.matches());

        template.sendBody("direct:foo", "Hello World");
        template.sendBody("direct:foo", "Hi World");
        template.sendBody("direct:foo", "A");
        assertEquals(false, event.matches());

        template.sendBody("direct:bar", "B");
        template.sendBody("direct:bar", "C");
        assertEquals(false, event.matches());

        template.sendBody("direct:bar", "Bye World");
        assertEquals(true, event.matches());

        template.sendBody("direct:foo", "D");
        template.sendBody("direct:bar", "Hey World");
        assertEquals(true, event.matches());
    }

    public void testFromFilterWhenExchangeDone() throws Exception {
        NotifyBuilder event = event()
            .from("direct:foo").filter(body().contains("World")).whenDone(3)
            .create();

        assertEquals(false, event.matches());

        template.sendBody("direct:foo", "Hello World");
        template.sendBody("direct:foo", "Hi World");
        template.sendBody("direct:foo", "A");
        assertEquals(false, event.matches());

        template.sendBody("direct:bar", "B");
        template.sendBody("direct:foo", "C");
        assertEquals(false, event.matches());

        template.sendBody("direct:bar", "Bye World");
        assertEquals(false, event.matches());

        template.sendBody("direct:bar", "D");
        template.sendBody("direct:foo", "Hey World");
        assertEquals(true, event.matches());

        template.sendBody("direct:bar", "E");
        template.sendBody("direct:foo", "Hi Again World");
        assertEquals(true, event.matches());
    }

    public void testFromFilterBuilderWhenExchangeDone() throws Exception {
        NotifyBuilder event = event()
            .filter().xpath("/person[@name='James']").whenDone(1)
            .create();

        assertEquals(false, event.matches());

        template.sendBody("direct:foo", "<person name='Claus'/>");
        assertEquals(false, event.matches());

        template.sendBody("direct:foo", "<person name='Jonathan'/>");
        assertEquals(false, event.matches());

        template.sendBody("direct:foo", "<person name='James'/>");
        assertEquals(true, event.matches());

        template.sendBody("direct:foo", "<person name='Hadrian'/>");
        assertEquals(true, event.matches());
    }

    public void testWhenExchangeCompleted() throws Exception {
        NotifyBuilder event = event()
            .whenCompleted(5)
            .create();

        assertEquals(false, event.matches());

        template.sendBody("direct:foo", "A");
        template.sendBody("direct:foo", "B");
        template.sendBody("direct:bar", "C");

        try {
            template.sendBody("direct:fail", "D");
        } catch (Exception e) {
            // ignore
        }

        try {
            template.sendBody("direct:fail", "E");
        } catch (Exception e) {
            // ignore
        }
        // should NOT be completed as it only counts successful exchanges
        assertEquals(false, event.matches());

        template.sendBody("direct:bar", "F");
        template.sendBody("direct:foo", "G");
        template.sendBody("direct:bar", "H");
        // now it should match
        assertEquals(true, event.matches());
    }

    public void testWhenExchangeExactlyDone() throws Exception {
        NotifyBuilder event = event()
            .whenExactlyDone(5)
            .create();

        assertEquals(false, event.matches());

        template.sendBody("direct:foo", "A");
        template.sendBody("direct:foo", "B");
        template.sendBody("direct:foo", "C");
        template.sendBody("direct:bar", "D");
        assertEquals(false, event.matches());

        template.sendBody("direct:bar", "E");
        assertEquals(true, event.matches());

        template.sendBody("direct:foo", "F");
        assertEquals(false, event.matches());
    }

    public void testWhenExchangeExactlyComplete() throws Exception {
        NotifyBuilder event = event()
            .whenExactlyCompleted(5)
            .create();

        assertEquals(false, event.matches());

        template.sendBody("direct:foo", "A");
        template.sendBody("direct:foo", "B");
        template.sendBody("direct:foo", "C");
        template.sendBody("direct:bar", "D");
        assertEquals(false, event.matches());

        template.sendBody("direct:bar", "E");
        assertEquals(true, event.matches());

        template.sendBody("direct:foo", "F");
        assertEquals(false, event.matches());
    }

    public void testWhenExchangeExactlyFailed() throws Exception {
        NotifyBuilder event = event()
            .whenExactlyFailed(2)
            .create();

        assertEquals(false, event.matches());

        template.sendBody("direct:foo", "A");
        template.sendBody("direct:foo", "B");
        template.sendBody("direct:foo", "C");

        try {
            template.sendBody("direct:fail", "D");
        } catch (Exception e) {
            // ignore
        }

        template.sendBody("direct:bar", "E");
        assertEquals(false, event.matches());

        try {
            template.sendBody("direct:fail", "F");
        } catch (Exception e) {
            // ignore
        }
        assertEquals(true, event.matches());

        template.sendBody("direct:bar", "G");
        assertEquals(true, event.matches());

        try {
            template.sendBody("direct:fail", "H");
        } catch (Exception e) {
            // ignore
        }
        assertEquals(false, event.matches());
    }

    public void testWhenAnyReceivedMatches() throws Exception {
        NotifyBuilder event = event()
            .whenAnyReceivedMatches(body().contains("Camel"))
            .create();

        assertEquals(false, event.matches());

        template.sendBody("direct:foo", "Hello World");
        assertEquals(false, event.matches());

        template.sendBody("direct:foo", "Bye World");
        assertEquals(false, event.matches());

        template.sendBody("direct:bar", "Hello Camel");
        assertEquals(true, event.matches());
    }

    public void testWhenAllReceivedMatches() throws Exception {
        NotifyBuilder event = event()
            .whenAllReceivedMatches(body().contains("Camel"))
            .create();

        assertEquals(false, event.matches());

        template.sendBody("direct:foo", "Hello Camel");
        assertEquals(true, event.matches());

        template.sendBody("direct:foo", "Bye Camel");
        assertEquals(true, event.matches());

        template.sendBody("direct:bar", "Hello World");
        assertEquals(false, event.matches());
    }

    public void testWhenAnyDoneMatches() throws Exception {
        NotifyBuilder event = event()
            .whenAnyDoneMatches(body().contains("Bye"))
            .create();

        assertEquals(false, event.matches());

        template.sendBody("direct:foo", "Hi World");
        assertEquals(false, event.matches());

        template.sendBody("direct:cake", "Camel");
        assertEquals(true, event.matches());

        template.sendBody("direct:foo", "Damn World");
        assertEquals(true, event.matches());
    }

    public void testWhenAllDoneMatches() throws Exception {
        NotifyBuilder event = event()
            .whenAllDoneMatches(body().contains("Bye"))
            .create();

        assertEquals(false, event.matches());

        template.sendBody("direct:cake", "Camel");
        assertEquals(true, event.matches());

        template.sendBody("direct:cake", "World");
        assertEquals(true, event.matches());

        template.sendBody("direct:foo", "Hi World");
        assertEquals(false, event.matches());
    }

    public void testWhenBodiesReceived() throws Exception {
        NotifyBuilder event = event()
            .whenBodiesReceived("Hi World", "Hello World")
            .create();

        assertEquals(false, event.matches());

        template.sendBody("direct:foo", "Hi World");
        assertEquals(false, event.matches());

        template.sendBody("direct:foo", "Hello World");
        assertEquals(true, event.matches());

        // should keep being true
        template.sendBody("direct:foo", "Bye World");
        assertEquals(true, event.matches());

        template.sendBody("direct:foo", "Damn World");
        assertEquals(true, event.matches());
    }

    public void testWhenBodiesDone() throws Exception {
        NotifyBuilder event = event()
            .whenBodiesDone("Bye World", "Bye Camel")
            .create();

        assertEquals(false, event.matches());

        template.requestBody("direct:cake", "World");
        assertEquals(false, event.matches());

        template.sendBody("direct:cake", "Camel");
        assertEquals(true, event.matches());

        // should keep being true
        template.sendBody("direct:foo", "Damn World");
        assertEquals(true, event.matches());
    }

    public void testWhenExactBodiesReceived() throws Exception {
        NotifyBuilder event = event()
            .whenExactBodiesReceived("Hi World", "Hello World")
            .create();

        assertEquals(false, event.matches());

        template.sendBody("direct:foo", "Hi World");
        assertEquals(false, event.matches());

        template.sendBody("direct:foo", "Hello World");
        assertEquals(true, event.matches());

        // should not keep being true
        template.sendBody("direct:foo", "Bye World");
        assertEquals(false, event.matches());

        template.sendBody("direct:foo", "Damn World");
        assertEquals(false, event.matches());
    }

    public void testWhenExactBodiesDone() throws Exception {
        NotifyBuilder event = event()
            .whenExactBodiesDone("Bye World", "Bye Camel")
            .create();

        assertEquals(false, event.matches());

        template.requestBody("direct:cake", "World");
        assertEquals(false, event.matches());

        template.sendBody("direct:cake", "Camel");
        assertEquals(true, event.matches());

        // should NOT keep being true
        template.sendBody("direct:foo", "Damn World");
        assertEquals(false, event.matches());
    }

    public void testWhenReceivedSatisfied() throws Exception {
        // lets use a mock to set the expressions as it got many great assertions for that
        // notice we use mock:assert which does NOT exist in the route, its just a pseudo name
        MockEndpoint mock = getMockEndpoint("mock:assert");
        mock.expectedBodiesReceivedInAnyOrder("Hello World", "Bye World", "Hi World");

        NotifyBuilder event = event()
            .from("direct:foo").whenDoneSatisfied(mock)
            .create();

        assertEquals(false, event.matches());

        template.sendBody("direct:foo", "Bye World");
        assertEquals(false, event.matches());

        template.sendBody("direct:foo", "Hello World");
        assertEquals(false, event.matches());

        // the event  is based on direct:foo so sending to bar should not trigger match
        template.sendBody("direct:bar", "Hi World");
        assertEquals(false, event.matches());

        template.sendBody("direct:foo", "Hi World");
        assertEquals(true, event.matches());
    }

    public void testWhenReceivedNotSatisfied() throws Exception {
        // lets use a mock to set the expressions as it got many great assertions for that
        // notice we use mock:assert which does NOT exist in the route, its just a pseudo name
        MockEndpoint mock = getMockEndpoint("mock:assert");
        mock.expectedMessageCount(2);
        mock.message(1).body().contains("Camel");

        NotifyBuilder event = event()
            .from("direct:foo").whenReceivedNotSatisfied(mock)
            .create();

        // is always false to start with
        assertEquals(false, event.matches());

        template.sendBody("direct:foo", "Bye World");
        assertEquals(true, event.matches());

        template.sendBody("direct:foo", "Hello Camel");
        assertEquals(false, event.matches());
    }

    public void testWhenNotSatisfiedUsingSatisfied() throws Exception {
        // lets use a mock to set the expressions as it got many great assertions for that
        // notice we use mock:assert which does NOT exist in the route, its just a pseudo name
        MockEndpoint mock = getMockEndpoint("mock:assert");
        mock.expectedMessageCount(2);
        mock.message(1).body().contains("Camel");

        NotifyBuilder event = event()
            .from("direct:foo").whenReceivedSatisfied(mock)
            .create();

        assertEquals(false, event.matches());

        template.sendBody("direct:foo", "Bye World");
        assertEquals(false, event.matches());

        template.sendBody("direct:foo", "Hello Camel");
        assertEquals(true, event.matches());
    }

    public void testComplexOrCamel() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:assert");
        mock.expectedBodiesReceivedInAnyOrder("Hello World", "Bye World", "Hi World");

        NotifyBuilder event = event()
            .from("direct:foo").whenReceivedSatisfied(mock)
            .and().from("direct:bar").whenExactlyDone(5).whenAnyReceivedMatches(body().contains("Camel"))
            .create();

        assertEquals(false, event.matches());

        template.sendBody("direct:foo", "Bye World");
        assertEquals(false, event.matches());

        template.sendBody("direct:foo", "Hello World");
        assertEquals(false, event.matches());

        // the event  is based on direct:foo so sending to bar should not trigger match
        template.sendBody("direct:bar", "Hi World");
        assertEquals(false, event.matches());

        template.sendBody("direct:foo", "Hi World");
        assertEquals(false, event.matches());

        template.sendBody("direct:bar", "Hi Camel");
        assertEquals(false, event.matches());

        template.sendBody("direct:bar", "A");
        template.sendBody("direct:bar", "B");
        template.sendBody("direct:bar", "C");
        assertEquals(true, event.matches());
    }

    public void testWhenDoneSatisfied() throws Exception {
        // lets use a mock to set the expressions as it got many great assertions for that
        // notice we use mock:assert which does NOT exist in the route, its just a pseudo name
        MockEndpoint mock = getMockEndpoint("mock:assert");
        mock.expectedBodiesReceived("Bye World", "Bye Camel");

        NotifyBuilder event = event()
            .whenDoneSatisfied(mock)
            .create();

        // is always false to start with
        assertEquals(false, event.matches());

        template.requestBody("direct:cake", "World");
        assertEquals(false, event.matches());

        template.requestBody("direct:cake", "Camel");
        assertEquals(true, event.matches());

        template.requestBody("direct:cake", "Damn");
        // will still be true as the mock has been completed
        assertEquals(true, event.matches());
    }

    public void testWhenDoneNotSatisfied() throws Exception {
        // lets use a mock to set the expressions as it got many great assertions for that
        // notice we use mock:assert which does NOT exist in the route, its just a pseudo name
        MockEndpoint mock = getMockEndpoint("mock:assert");
        mock.expectedBodiesReceived("Bye World", "Bye Camel");

        NotifyBuilder event = event()
            .whenDoneNotSatisfied(mock)
            .create();

        // is always false to start with
        assertEquals(false, event.matches());

        template.requestBody("direct:cake", "World");
        assertEquals(true, event.matches());

        template.requestBody("direct:cake", "Camel");
        assertEquals(false, event.matches());

        template.requestBody("direct:cake", "Damn");
        // will still be false as the mock has been completed
        assertEquals(false, event.matches());
    }

    public void testReset() throws Exception {
        NotifyBuilder event = event()
            .whenExactlyDone(1)
            .create();

        template.sendBody("direct:foo", "Hello World");
        assertEquals(true, event.matches());

        template.sendBody("direct:foo", "Bye World");
        assertEquals(false, event.matches());

        // reset
        event.reset();
        assertEquals(false, event.matches());

        template.sendBody("direct:foo", "Hello World");
        assertEquals(true, event.matches());

        template.sendBody("direct:foo", "Bye World");
        assertEquals(false, event.matches());
    }

    public void testResetBodiesReceived() throws Exception {
        NotifyBuilder event = event()
            .whenBodiesReceived("Hello World", "Bye World")
            .create();

        template.sendBody("direct:foo", "Hello World");
        template.sendBody("direct:foo", "Bye World");
        assertEquals(true, event.matches());

        // reset
        event.reset();
        assertEquals(false, event.matches());

        template.sendBody("direct:foo", "Hello World");
        assertEquals(false, event.matches());

        template.sendBody("direct:foo", "Bye World");
        assertEquals(true, event.matches());
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:foo").routeId("foo").to("mock:foo");

                from("direct:bar").routeId("bar").to("mock:bar");

                from("direct:fail").routeId("fail").throwException(new IllegalArgumentException("Damn"));

                from("direct:cake").routeId("cake").transform(body().prepend("Bye ")).to("log:cake");

                from("direct:beer").routeId("beer").to("log:beer").to("mock:beer");
            }
        };
    }
}
