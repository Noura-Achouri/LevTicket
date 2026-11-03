import request from './request'

export function login(email, password) {
  return request.post('/auth/login', { email, password })
}

export function register(username, email, password, phone) {
  return request.post('/auth/register', { username, email, password, phone })
}