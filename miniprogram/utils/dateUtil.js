/**
 * 日期工具类
 * 提供iOS兼容的日期格式化功能
 */

/**
 * 将日期字符串转换为iOS兼容格式
 * @param {string} timeStr - 原始时间字符串
 * @returns {string} - iOS兼容的时间字符串
 */
function toIOSCompatibleFormat(timeStr) {
  if (!timeStr) return '';
  
  // iOS兼容性处理：将 "yyyy-MM-dd HH:mm:ss" 格式转换为 "yyyy/MM/dd HH:mm:ss"
  return timeStr.replace(/(\d{4})-(\d{2})-(\d{2})/, '$1/$2/$3');
}

/**
 * 安全的日期创建函数
 * @param {string} timeStr - 时间字符串
 * @returns {Date|null} - Date对象或null
 */
function createSafeDate(timeStr) {
  if (!timeStr) return null;
  
  try {
    const isoTimeStr = toIOSCompatibleFormat(timeStr);
    const date = new Date(isoTimeStr);
    
    // 检查日期是否有效
    if (isNaN(date.getTime())) {
      console.warn('无效的日期格式:', timeStr);
      return null;
    }
    
    return date;
  } catch (error) {
    console.error('日期创建错误：', error);
    return null;
  }
}

/**
 * 格式化时间为相对时间（如：刚刚、5分钟前等）
 * @param {string} timeStr - 时间字符串
 * @returns {string} - 格式化后的时间
 */
function formatRelativeTime(timeStr) {
  const date = createSafeDate(timeStr);
  if (!date) return timeStr;
  
  const now = new Date();
  const diff = now - date;
  
  if (diff < 60000) { // 1分钟内
    return '刚刚';
  } else if (diff < 3600000) { // 1小时内
    return Math.floor(diff / 60000) + '分钟前';
  } else if (diff < 86400000) { // 1天内
    return Math.floor(diff / 3600000) + '小时前';
  } else {
    return date.toLocaleDateString();
  }
}

/**
 * 格式化时间为标准格式（yyyy-MM-dd HH:mm）
 * @param {string} timeStr - 时间字符串
 * @returns {string} - 格式化后的时间
 */
function formatStandardTime(timeStr) {
  const date = createSafeDate(timeStr);
  if (!date) return timeStr;
  
  const year = date.getFullYear();
  const month = padZero(date.getMonth() + 1);
  const day = padZero(date.getDate());
  const hours = padZero(date.getHours());
  const minutes = padZero(date.getMinutes());
  
  return `${year}-${month}-${day} ${hours}:${minutes}`;
}

/**
 * 格式化时间为简短格式（MM-dd HH:mm）
 * @param {string} timeStr - 时间字符串
 * @returns {string} - 格式化后的时间
 */
function formatShortTime(timeStr) {
  const date = createSafeDate(timeStr);
  if (!date) return timeStr;
  
  return date.toLocaleDateString() + ' ' + date.toLocaleTimeString().slice(0, 5);
}

/**
 * 数字补零
 * @param {number} num - 数字
 * @returns {string} - 补零后的字符串
 */
function padZero(num) {
  return num < 10 ? '0' + num : num.toString();
}

/**
 * 获取当前时间戳
 * @returns {number} - 时间戳
 */
function getCurrentTimestamp() {
  return Date.now();
}

/**
 * 判断是否为今天
 * @param {string} timeStr - 时间字符串
 * @returns {boolean} - 是否为今天
 */
function isToday(timeStr) {
  const date = createSafeDate(timeStr);
  if (!date) return false;
  
  const today = new Date();
  return date.toDateString() === today.toDateString();
}

/**
 * 判断是否为昨天
 * @param {string} timeStr - 时间字符串
 * @returns {boolean} - 是否为昨天
 */
function isYesterday(timeStr) {
  const date = createSafeDate(timeStr);
  if (!date) return false;
  
  const yesterday = new Date();
  yesterday.setDate(yesterday.getDate() - 1);
  return date.toDateString() === yesterday.toDateString();
}

module.exports = {
  toIOSCompatibleFormat,
  createSafeDate,
  formatRelativeTime,
  formatStandardTime,
  formatShortTime,
  padZero,
  getCurrentTimestamp,
  isToday,
  isYesterday
};
