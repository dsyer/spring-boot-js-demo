[Unpoly](https://unpoly.com/) is a JavaScript library supporting hypermedia-style applications (a bit like [HTMX]([https://](https://htmx.org/))). It is a small piece of code that makes it easy to build modern web applications with less code and more fun. Unpoly is open source and free to use in your projects.

The implementation of this sample is a bit different than the others in this repository for a few reasons:

* Unpoly [doesn't support ESM](https://github.com/unpoly/unpoly/issues/136) so we just revert to using the UMD version of the library, and other libraries.
* Unpoly has no out-of-the-box support for SSE, so we use a polling mechanism to check for new messages instead. The `stream` endpoint is semantically different to the other samples as a result - it is used to retrieve the latest messages, rather than to subscribe to a stream of messages.

