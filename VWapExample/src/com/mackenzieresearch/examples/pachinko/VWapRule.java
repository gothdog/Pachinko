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

import com.mackenzieresearch.pachinko.DefaultCARule;
import com.mackenzieresearch.pachinko.Variable;
import com.mackenzieresearch.roux.context.Window;
import com.mackenzieresearch.roux.kernel.*;

import java.nio.file.WatchEvent;
import java.util.Collection;

import static util.RuntimeAssertion.assertNotNull;
import static util.RuntimeAssertion.assertTrue;

public class VWapRule extends DefaultCARule<StockTradeEvent> {
  String _channelName = null;
  long _windowSize = 0;
  int _event = -1;
  int _window = -1;
  int _volume = -1;
  int _total = -1;
  int _vwap = -1;

  public VWapRule(String stockChannelName, long windowSize) {
    assertNotNull(stockChannelName);
    _channelName = stockChannelName;
    _event = addVariable(new Variable<WatchEvent>(_channelName, null));

    assertTrue(windowSize > 0);
    _windowSize = windowSize;

    _window = addOptionalVariable(new Window<StockTradeEvent>(_channelName + "_window"));
    _volume = addOptionalVariable(new Variable<Integer>(_channelName + "_volume", 0));
    _total = addOptionalVariable(new Variable<Double>(_channelName + "_total", 0.0));
    _vwap = addOptionalVariable(new Variable<Double>(_channelName + "_vwap", 0.0));
  }

  @Override
  public boolean evaluateCondition(IMonadex<? extends IReadOnlyMonad> context) {
    StockTradeEvent event = (StockTradeEvent) context.bindValue(_event);

    if (_channelName.equalsIgnoreCase(event.getSymbol()))
      return true;
    else
      return false;
  }

  @Override
  public void doAction(IReadWriteMonadex<? extends IMonad> context) {
    StockTradeEvent event = (StockTradeEvent) context.bindValue(_event);
    //  Get the monad for our sliding window because it's more efficient to do so than copy collections around:
    IWindow<StockTradeEvent> window = (IWindow<StockTradeEvent>) context.getMonad(_window);
    Integer volume = (Integer) context.bindValue(_volume);
    Double total = (Double) context.bindValue(_total);

    //  First expire any events that the window has slid beyond...
    Collection<EventTick<StockTradeEvent>> expired = window.expire(event.getTimeTick() - _windowSize, context);
    for (EventTick expiredTick : expired) {
      StockTradeEvent expiredEvent = (StockTradeEvent)expiredTick.event;
      volume = volume - expiredEvent.getShares();
      total = total - (expiredEvent.getShares() * expiredEvent.getSharePrice());
    }

    //  Now add in the new event to our window and calculate the vwap...
    window.append(event.getTimeTick(), event, context);
    volume = volume + event.getShares();
    total = total + (event.getShares() * event.getSharePrice());
    Double vwap = total / volume;

    //  Finally return any new values to their respective monads so that other calculations can react to them...
    context.returnValue(_volume, volume);
    context.returnValue(_total, total);
    context.returnValue(_vwap, vwap);
  }
}
