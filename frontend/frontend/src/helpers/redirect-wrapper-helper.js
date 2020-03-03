import React from "react";
import RedirectWrapper from "../components/common/redirect-wrapper"

function withRedirect(component, uri) {
    return <RedirectWrapper uri={uri}>{component}</RedirectWrapper>;
}

export default withRedirect;