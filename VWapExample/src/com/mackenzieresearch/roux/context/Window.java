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

package com.mackenzieresearch.roux.context;

import com.mackenzieresearch.roux.kernel.*;

import java.util.ArrayList;
import java.util.Collection;

public class Window<T> extends Monad<Collection<EventTick<T>>> implements IWindow<T> {
  public Window(String name) {
    super(name);
    _value = new ArrayList<EventTick<T>>();
  }

  public Window(String name, int key) {
    super(name, key);
    _value = new ArrayList<EventTick<T>>();
  }

  @Override
  public int size(IMonadex<? extends IMonad> context) {
    return _value.size();
  }

  @Override
  public boolean isEmpty(IMonadex<? extends IMonad> context) {
    return _value.isEmpty();
  }

  @Override
  public void clear() {
    _value.clear();
  }

  @Override
  public void append(long timeTick, T event, IMonadex<? extends IMonad> context) {
    ((ArrayList<EventTick<T>>) _value).add(new EventTick<T>(timeTick, event));
    for (IListener listener : _changeListeners)
      listener.changed(this, context);
  }

  @Override
  public Collection<EventTick<T>> expire(long timeTick, IMonadex<? extends IMonad> context) {
    Collection<EventTick<T>> expired = new ArrayList<EventTick<T>>();
    ArrayList<EventTick<T>> window = (ArrayList<EventTick<T>>) _value;

    for (; ; ) {
      if (window.size() > 0) {
        EventTick<T> event = window.get(0);
        if (event.timeTick <= timeTick) {
          expired.add(event);
          window.remove(0);
        } else
          break;
      } else
        break;
    }

    for (IListener listener : _changeListeners)
      listener.changed(this, context);

    return expired;
  }
}
