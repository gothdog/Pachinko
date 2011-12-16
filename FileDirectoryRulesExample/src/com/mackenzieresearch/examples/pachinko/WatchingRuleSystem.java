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
import com.mackenzieresearch.pachinko.ICARule;
import com.mackenzieresearch.pachinko.Variable;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

public class WatchingRuleSystem extends CARuleSystem<WatchEvent> implements Runnable {
  protected Map<String, WatchService> _eventSources = new HashMap<String, WatchService>();
  protected volatile boolean _done = false;

  public WatchingRuleSystem(ICARule<WatchEvent>... rules) {
    super(rules);
  }

  public WatchingRuleSystem addEventSource(String channelName, WatchService watcher) {
    _eventSources.put(channelName, watcher);
    defineVariable(new Variable<WatchEvent>(channelName, null));
    return this;
  }

  public WatchingRuleSystem addEventSource(String channelName, String filePath, WatchEvent.Kind<?>... eventKinds) throws IOException {
    WatchService watcher = FileSystems.getDefault().newWatchService();
    Path path = FileSystems.getDefault().getPath(filePath);
    path.register(watcher, eventKinds);
    addEventSource(channelName, watcher);
    return this;
  }

  public void poll() {
    for (String channelName : _eventSources.keySet()) {
      WatchKey wkey = _eventSources.get(channelName).poll();
      if (wkey != null) {
        for (WatchEvent<?> event : wkey.pollEvents()) {
          _alphaMemory.returnValue(channelName, event);
        }

        if (!wkey.reset())
          break;
      }
    }

    executeActivations();
  }

  public void run() {
    while (!_done)
      poll();
    _done = false;
  }

  public void start() {
    new Thread(this).start();
  }

  public void stop() {
    _done = true;
  }
}
