import {useEffect, useRef} from "react";

export default function useDidMountHook(callback) {
    const didMount = useRef(null)

    useEffect(() => {
        if (callback && !didMount.current) {
            didMount.current = true
            callback()
        }
    })
}