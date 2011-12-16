/*
   Example extensions of PACHINKO, A fast, embeddable micro-rule engine.
   Version 0.2

   Copyright 2011 Kenneth R. Mackenzie (www.mackenzieresearch.com)

   This program is free software: you can redistribute it and/or modify
   it under the terms of Version 3 of the GNU Affero General Public
   License as published by the Free Software Foundation.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Affero General Public License for more details.

   You should have received a copy of the GNU Affero General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.

   If you require a version of this software which can be used as
   part of a commercial for-profit program, please contact Mackenzie
   Research for a commercial license.

   Unless required by applicable law or agreed to in writing, this
   software is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
   CONDITIONS OF ANY KIND, either express or implied.

   See the License for the specific language governing permissions and
   limitations under the License.
*/

package com.mackenzieresearch.examples.pachinko;

import com.mackenzieresearch.pachinko.CARuleSystem;
import com.mackenzieresearch.roux.kernel.IReadWriteMonadex;
import com.mackenzieresearch.roux.kernel.IWindow;
import org.junit.Test;

import java.util.Random;

import static util.RuntimeAssertion.assertTrue;

/**
 * The VWAP or Volume-Weighted Average Price is a sliding-window calculation widely used in the financial
 * trading community.  Commercial CEP engines often describe their performance capabilities using VWAP
 * calculation as a benchmark.
 *
 * So this example illustrates a couple of interesting things:
 *
 * -  An extension to Monad which is a sliding window
 *
 * -  A sample rule that implements a VWAP calculation using our new sliding window Monad
 *
 * -  Some JUnit tests that exercise this rule in various ways, showing its function and performance.
 * 
 */
public class VWapExample {
  private Random _random = new Random();

  @Test
  public void simpleSingleThreadedVWap() {
    //  Initialize rule system with its set of rules:
    CARuleSystem<StockTradeEvent> ruleSystem = new CARuleSystem<StockTradeEvent>(new VWapRule("MACK", 10));
    IReadWriteMonadex context = ruleSystem.freeVariables();
    int volumeIndex = context.getIndex("MACK_volume");
    int totalIndex = context.getIndex("MACK_total");
    int vwapIndex = context.getIndex("MACK_vwap");

    //  Since our window size is set to 10, spin thru 10 events and compare the VWap calculated by the rule
    //  with our own VWap calculation to see if we get the same numbers.  We only do 10 so that we don't have
    //  to replicate the sliding window behavior of IWindow here in our test code...
    for (int tick = 1; tick <= 10; tick++) {
      double vwap = (Double) context.bindValue(vwapIndex);
      int volume = (Integer) context.bindValue(volumeIndex);
      double total = (Double) context.bindValue(totalIndex);

      //  Create a random event for the stock symbol "MACK":
      StockTradeEvent newEvent = _generateEvent(tick, "MACK");
      //  Pass the event to the engine by setting it into the monad in the context:
      context.returnValue("MACK", newEvent);
      //  Allow any rules that have become activated by this event to fire:
      ruleSystem.executeActivations();

      //  Check the resulting values in the context to see if we believe the calculations are correct...
      volume = volume + newEvent.getShares();
      assertTrue(volume == ((Integer) context.bindValue(volumeIndex)));
      total = total + (newEvent.getShares() * newEvent.getSharePrice());
      assertTrue(total == (Double) context.bindValue(totalIndex));
      assertTrue(((total / volume) - (Double) context.bindValue(vwapIndex)) < 0.01);
    }
  }

  /**
   * This is a quick-and-dirty performance test.  It isn't intended as an exhaustive benchmark bid for bragging
   * rights.  Rather it should give some reassurance that even single-threaded, this approach to rule and event processing
   * can yield satisfactory performance numbers.
   *
   * While stock trades could be arranged with all symbols aggregated on a simple channel, this simple example is
   * arranged so that each stock is its own channel.  This allows us to provide higher performance and simpler, more
   * explicit processing code so long as we have sufficient memory.
   */
  @Test
  public void singleThreadedVWapPerfTest() {
    //  Initialize rule system with its set of rules:
    CARuleSystem<StockTradeEvent> ruleSystem = new CARuleSystem<StockTradeEvent>(new VWapRule("MACK", 10));
    IReadWriteMonadex context = ruleSystem.freeVariables();
    int eventIndex = context.getIndex("MACK");
    int volumeIndex = context.getIndex("MACK_volume");
    int totalIndex = context.getIndex("MACK_total");
    int vwapIndex = context.getIndex("MACK_vwap");

    //  Preload a random pattern of events to reduce perf cost of generating events during processing
    StockTradeEvent[] events = _preloadEventStream(0, "MACK", 10000);

    //  Warm up Hotspot VM for more accurate measurement...
    int eventStreamCounter = 0;
    for (int ticker = 0; ticker < 10000; ticker++) {
      context.returnValue(eventIndex, events[eventStreamCounter]);
      ruleSystem.executeActivations();

      eventStreamCounter++;
      if (eventStreamCounter >= 10000)
        eventStreamCounter = 0;
    }

    //  Now do it for real...
    events = _preloadEventStream(10001, "MACK", 10000);
    long startTime = System.nanoTime();

    eventStreamCounter = 0;
    for (int ticker = 0; ticker < 10000; ticker++) {
      context.returnValue(eventIndex, events[eventStreamCounter]);
      ruleSystem.executeActivations();

      eventStreamCounter++;
      if (eventStreamCounter >= 10000)
        eventStreamCounter = 0;
    }

    long elapsed = System.nanoTime() - startTime;
    System.out.println("Elapsed: " + elapsed + ".  Avg: " + (elapsed / 10000));
    System.out.println("Size of window: " + ((IWindow)context.getMonad("MACK_window")).size(context));
  }

  private StockTradeEvent _generateEvent(long timeTick, String symbol) {
    return new StockTradeEvent(timeTick, symbol, _random.nextInt(100), _random.nextDouble());
  }

  private StockTradeEvent[] _preloadEventStream(long initialTimeTick, String symbol, int qty) {
    StockTradeEvent[] events = new StockTradeEvent[qty];
    for (int tick = 0; tick < qty; tick++) {
      events[tick] = _generateEvent(initialTimeTick + tick, symbol);
    }

    return events;
  }
}
