import request from './request'

export function getAuctionByTicket(ticketId) {
  return request.get(`/auctions/ticket/${ticketId}`)
}

export function getBidHistory(auctionId) {
  return request.get(`/auctions/${auctionId}/bids`)
}

export function placeBid(auctionId, amount) {
  return request.post(`/auctions/${auctionId}/bids`, { amount })
}

export function closeAuction(auctionId) {
  return request.post(`/auctions/${auctionId}/close`)
}