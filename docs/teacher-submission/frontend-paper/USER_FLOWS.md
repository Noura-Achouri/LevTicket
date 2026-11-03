# Frontend User Flows

## 1. Anonymous visitor discovers a ticket

1. The user opens `/`.
2. The home page requests active tickets from `GET /api/tickets`.
3. The user enters a keyword and presses Search.
4. The frontend navigates to `/search?keyword=...`.
5. The search view loads categories and filtered tickets.
6. The user selects a ticket card.
7. The frontend navigates to `/auction/:id`.

## 2. User registers

1. The user opens `/register`.
2. The user enters username, email, password, and phone.
3. The form calls `POST /api/auth/register`.
4. On success, the user is redirected to `/login`.
5. On failure, an Element Plus error message is shown.

## 3. User logs in

1. The user opens `/login`.
2. The user submits email and password.
3. The form calls `POST /api/auth/login`.
4. The returned token is saved by the Pinia authentication store.
5. The token is persisted in `localStorage`.
6. The user summary is stored in memory.
7. The user is redirected to `/`.

## 4. Buyer places a bid

1. The buyer opens an auction detail page.
2. The frontend loads ticket data.
3. The frontend loads the auction by ticket ID.
4. The frontend loads bid history.
5. The bid input starts at one unit above the current bid.
6. The buyer submits a bid.
7. The frontend calls `POST /api/auctions/{auctionId}/bids`.
8. On success, the current bid and bid history are refreshed.
9. On failure, the user receives an error message.

## 5. Seller publishes a ticket

1. The seller opens `/post-ticket`.
2. The frontend loads categories.
3. The seller enters event and auction information.
4. The seller selects up to five images.
5. The frontend builds a `FormData` payload.
6. The frontend calls `POST /api/tickets` with multipart content.
7. On success, the seller is sent to the created auction page.

## 6. Seller manages listings

1. The seller opens `/my-listings`.
2. The frontend requests the seller's active tickets.
3. The seller switches between Active, Sold, and Expired tabs.
4. The selected status is sent as a query parameter.
5. Selecting View opens the auction detail route.

## 7. User reviews orders

1. The user opens `/orders`.
2. Bought orders are loaded first.
3. The user switches to Sold when needed.
4. Completed orders without a review show Leave Review.
5. The review modal collects star rating and comment.
6. The review is sent to `POST /api/reviews`.
7. The order list is refreshed after submission.

## 8. User views profile trust information

1. The user opens `/profile`.
2. The frontend requests profile data.
3. The page shows biography, trust score, auction statistics, and reviews.
4. Empty review lists display an empty state.

## 9. User asks the AI assistant for help

1. The user opens the global customer-service drawer or `/ai-chat`.
2. The user selects a quick question or types a message.
3. The frontend builds a conversation history.
4. The message is sent to `POST /api/ai/complaint`.
5. A typing indicator is displayed while waiting.
6. The returned `reply` is added to the conversation.
7. Network and timeout errors are translated into readable messages.

## 10. Important failure states

The frontend includes visible handling for:

- Empty search results.
- Loading ticket, auction, order, and profile data.
- Failed login, ticket submission, bidding, and AI calls.
- Missing active auction before placing a bid.
- AI timeout or backend connection refusal.
- No reviews available for a profile.
