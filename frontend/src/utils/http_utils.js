import {isStringNullOrEmpty} from './string_utils';

export function getBackendAddress() {
  let protocol = process.env.REACT_APP_BACKEND_PROTOCOL;
  let url = process.env.REACT_APP_BACKEND_URL;
  let port = isStringNullOrEmpty(process.env.REACT_APP_BACKEND_PORT)
      ? '' : `:${process.env.REACT_APP_BACKEND_PORT}`;
  return `${protocol}://${url}${port}`;
}