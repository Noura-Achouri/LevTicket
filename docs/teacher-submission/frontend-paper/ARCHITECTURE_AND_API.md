# Architecture and API Appendix

## 1. Frontend-to-backend relationship

The frontend is a browser client. It does not directly access the database. It sends HTTP requests to the Spring Boot backend and renders the returned data.

```text
Browser
  |
  | Vue components and Vue Router
  v
Pinia authentication state + feature API modules
  |
  | Axios, bearer token, JSON or multipart/form-data
  v
Spring Boot backend at http://localhost:8080/api
  |
  v
Business services, authentication, auctions, orders, database, AI service
```

## 2. API modules

| Frontend module | Methods | Backend routes |
| --- | --- | --- |
| `auth.js` | `login`, `register` | `POST /auth/login`, `POST /auth/register` |
| `tickets.js` | `getTickets`, `getTicketById`, `createTicket`, `getMyTickets` | `GET /tickets`, `GET /tickets/{id}`, `POST /tickets`, `GET /tickets/my` |
| `auctions.js` | `getAuctionByTicket`, `getBidHistory`, `placeBid` | `GET /auctions/ticket/{ticketId}`, `GET /auctions/{auctionId}/bids`, `POST /auctions/{auctionId}/bids` |
| `orders.js` | `getOrders`, `getOrderById` | `GET /orders`, `GET /orders/{id}` |
| `categories.js` | `getCategories` | `GET /categories` |
| `users.js` | `getUserProfile`, `updateMyProfile`, `uploadAvatar`, `getTrustScore` | `GET /users/{id}`, `PUT /users/me`, `POST /users/me/avatar`, `GET /users/{id}/trust-score` |
| `reviews.js` | `submitReview` | `POST /reviews` |
| `ai.js` | `getRecommendations`, `getPriceInsight`, `submitComplaint` | `POST /ai/recommend`, `GET /ai/price-insight/{ticketId}`, `POST /ai/complaint` |

## 3. Request behavior

The Axios instance uses the base URL `http://localhost:8080/api` and a 65-second timeout. Before each request, the current Pinia token is read. When present, the request receives an Authorization header using the bearer-token format.

The response interceptor returns `response.data`, so feature modules consume the backend envelope without repeating Axios response handling. Errors prefer the backend error body when one is available.

## 4. Data formats used by the interface

The views expect data shaped around the following concepts:

- Ticket: `id`, `eventName`, `eventDate`, `eventLocation`, `seatInfo`, `description`, `categoryName`, `tags`, `startPrice`, `currentBid`, and `status`.
- Auction: `id`, `currentBid`, and `endTime`.
- Seller: `username` and `trustScore`.
- Bid: `bidderUsername`, `amount`, and `bidTime`.
- Order: `ticketEventName`, `finalPrice`, `status`, `createTime`, and `reviewLeft`.
- Profile: `username`, `bio`, `trustScore`, and `reviews`.
- Authentication response: `token`, `userId`, and `username`.
- AI response: `reply`.

The backend remains the source of truth for validation and business rules.

## 5. Mock-data mode

Set the Vite environment variable below to enable mock behavior where supported:

```text
VITE_USE_MOCKS=true
```

The mock modules are located at `frontend/src/api/mocks/`. Mock support is currently strongest for ticket lists and details, while authentication, bidding, orders, reviews, and AI actions normally require backend routes.

## 6. Architectural strengths

- Feature API code is separate from visual components.
- Routes are lazy-loaded.
- Authentication header behavior is centralized.
- Pinia is used only for cross-screen authentication state.
- UI components use Element Plus for consistent forms, tables, and feedback.
- Mock modules permit partial demonstrations without a live backend.

## 7. Architectural risks

- The API base URL is currently hard-coded in `request.js`.
- The router does not currently include authentication guards.
- Some profile functionality exists in the API module but is not exposed by the current profile screen.
- AI attachment, audio, and camera controls are present in the dedicated UI, but the current complaint API call sends text history rather than those media payloads.
- The fallback profile ID in `Profile.vue` is a temporary development fallback and should be replaced with a complete authenticated-user flow.
