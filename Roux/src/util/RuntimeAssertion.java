/*

   ROUX, A foundation library for simple monadic expressions.
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

package util;

import java.util.Collection;
import java.util.Map;

public class RuntimeAssertion extends RuntimeException {
  public static void assertParam(Object param) {
    if (param == null)
      throw new RuntimeAssertion("Parameter was null");
  }

  public static void assertParam(Object param, String msg) {
    if (param == null)
      throw new RuntimeAssertion("Parameter was null: " + msg);
  }

  public static void assertNotNull(Object param) {
    if (param == null)
      throw new RuntimeAssertion("Object was null");
  }

  public static void assertNotNull(Object param, String msg) {
    if (param == null)
      throw new RuntimeAssertion("Object was null: " + msg);
  }

  public static void assertTrue(boolean expr) {
    if (!expr)
      throw new RuntimeAssertion("Assertion failed");
  }

  public static void assertTrue(boolean expr, String msg) {
    if (!expr)
      throw new RuntimeAssertion("Assertion failed: " + msg);
  }

  public static void assertEqual(Object expected, Object actual) {
    if (expected != null) {
      if (!expected.equals(actual))
        throw new RuntimeAssertion("Expected: " + expected.toString() + ", Found: " + (actual != null ? actual.toString() : "null"));
    } else {
      if (actual != null)
        throw new RuntimeAssertion("Expected: null" + ", Found: " + actual.toString());
    }
  }

  public static void assertEqual(Object expected, Object actual, String msg) {
    if (expected != null) {
      if (!expected.equals(actual))
        throw new RuntimeAssertion("Expected: " + expected.toString() + ", Found: " + (actual != null ? actual.toString() : "null.  " + msg));
    } else {
      if (actual != null)
        throw new RuntimeAssertion("Expected: null" + ", Found: " + actual.toString() + msg);
    }
  }

  public static void assertNotEqual(Object expected, Object actual) {
    if (expected != null) {
      if (expected.equals(actual))
        throw new RuntimeAssertion("Expected: " + expected.toString() + ", Found: " + (actual != null ? actual.toString() : "null"));
    } else {
      if (actual != null)
        throw new RuntimeAssertion("Expected: null" + ", Found: " + actual.toString());
    }
  }

  public static void assertNotEqual(Object expected, Object actual, String msg) {
    if (expected != null) {
      if (expected.equals(actual))
        throw new RuntimeAssertion("Expected: " + expected.toString() + ", Found: " + (actual != null ? actual.toString() : "null.  " + msg));
    } else {
      if (actual != null)
        throw new RuntimeAssertion("Expected: null" + ", Found: " + actual.toString() + msg);
    }
  }

  public static void assertContains(Map<?, ?> map, Object key) {
    if (!map.containsKey(key))
      throw new RuntimeAssertion("Assertion failed.  Map did not contain key: " + key.toString());
  }

  public static void assertContains(Map<?, ?> map, Object key, String msg) {
    if (!map.containsKey(key))
      throw new RuntimeAssertion("Assertion failed.  Map did not contain key: " + key.toString() + msg);
  }

  public static void assertContains(Collection<?> list, Object object) {
    if (!list.contains(object))
      throw new RuntimeAssertion("Assertion failed.  List did not contain key: " + object.toString());
  }

  public static void assertContains(Collection<?> list, Object object, String msg) {
    if (!list.contains(object))
      throw new RuntimeAssertion("Assertion failed.  List did not contain key: " + object.toString() + " " + msg);
  }

  public static void assertDoesNotContain(Map<?, ?> map, Object key) {
    if (map.containsKey(key))
      throw new RuntimeAssertion("Assertion failed.  Map did not contain key: " + key.toString());
  }

  public static void assertDoesNotContain(Map<?, ?> map, Object key, String msg) {
    if (map.containsKey(key))
      throw new RuntimeAssertion("Assertion failed.  Map did not contain key: " + key.toString() + msg);
  }

  public static void assertDoesNotContain(Collection<?> list, Object object) {
    if (list.contains(object))
      throw new RuntimeAssertion("Assertion failed.  List did not contain key: " + object.toString());
  }

  public static void assertDoesNotContain(Collection<?> list, Object object, String msg) {
    if (list.contains(object))
      throw new RuntimeAssertion("Assertion failed.  List did not contain key: " + object.toString() + " " + msg);
  }

  public static void assertSize(Map<?, ?> map, int size) {
    if (!(map.size() == size))
      throw new RuntimeAssertion("Assertion failed.  Map was not size " + size);
  }

  public static void assertSize(Map<?, ?> map, int size, String msg) {
    if (!(map.size() == size))
      throw new RuntimeAssertion("Assertion failed.  Map was not size " + size + " " + msg);
  }

  public static void assertSize(Collection<?> list, int size) {
    if (!(list.size() == size))
      throw new RuntimeAssertion("Assertion failed.  List is not size " + size);
  }

  public static void assertSize(Collection<?> list, int size, String msg) {
    if (!(list.size() == size))
      throw new RuntimeAssertion("Assertion failed.  List is not size " + size + " " + msg);
  }

  public static void assertEmpty(Map<?, ?> map) {
    if (!map.isEmpty())
      throw new RuntimeAssertion("Assertion failed.  Map is not empty");
  }

  public static void assertEmpty(Map<?, ?> map, String msg) {
    if (!map.isEmpty())
      throw new RuntimeAssertion("Assertion failed.  Map is not empty.  " + msg);
  }

  public static void assertEmpty(Collection<?> list) {
    if (!list.isEmpty())
      throw new RuntimeAssertion("Assertion failed.  List is not empty");
  }

  public static void assertEmpty(Collection<?> list, String msg) {
    if (!list.isEmpty())
      throw new RuntimeAssertion("Assertion failed.  List is not empty.  " + msg);
  }

  public static void assertNotEmpty(Map<?, ?> map) {
    if (map.isEmpty())
      throw new RuntimeAssertion("Assertion failed.  Map is empty");
  }

  public static void assertNotEmpty(Map<?, ?> map, String msg) {
    if (map.isEmpty())
      throw new RuntimeAssertion("Assertion failed.  Map is empty.  " + msg);
  }

  public static void assertNotEmpty(Collection<?> list) {
    if (list.isEmpty())
      throw new RuntimeAssertion("Assertion failed.  List is empty");
  }

  public static void assertNotEmpty(Collection<?> list, String msg) {
    if (list.isEmpty())
      throw new RuntimeAssertion("Assertion failed.  List is empty.  " + msg);
  }

  public RuntimeAssertion() {
    super();
  }

  public RuntimeAssertion(String msg) {
    super(msg);
  }

  public RuntimeAssertion(String msg, Throwable throwable) {
    super(msg, throwable);
  }

  public RuntimeAssertion(Throwable throwable) {
    super(throwable);
  }
}
