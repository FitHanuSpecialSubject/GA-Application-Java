export function isStringNullOrEmpty(str) {
  if (typeof str === 'string') {
    return !str || str.length === 0;
  } else {
    return false
  }
}