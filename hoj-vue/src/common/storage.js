const localStorage = window.localStorage

export default {
  name: 'storage',

  /**
   * save value(Object) to key
   * @param {string} key 键
   * @param {Object} value 值
   */
  set (key, value) {
    localStorage.setItem(key, JSON.stringify(value))
  },

  /**
   * get value(Object) by key
   * @param {string} key 键
   * @return {Object}
   */
  get (key) {
    return JSON.parse(localStorage.getItem(key)) || null
  },

  /**
   * remove key from localStorage
   * @param {string} key 键
   */
  remove (key) {
    localStorage.removeItem(key)
  },
  /**
   * clear all
   */
  clear () {
    localStorage.clear()
  }
}
