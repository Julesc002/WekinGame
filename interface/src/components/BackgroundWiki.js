import axios from "axios";
import React, { useEffect } from 'react';
import { API_URL } from '../config';

function BackgroundWiki(props) {

    useEffect(() => {
        axios.get(`${API_URL}/wiki/${props.id}/background`).then((res) => {
            document.body.style.backgroundImage = `url(${res.data.url})`;
        }).catch((error) => {
            console.error(error);
        });
    }, [props]);
    

    return (
        <>

        </>
    )
}

export default BackgroundWiki;
