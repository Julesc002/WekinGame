import React from 'react';
import { Link } from "react-router-dom";

const MessageForbidden = () => {
    return (
        <div>
            <h1>Oh Mince, vous n'avez pas les droits pour accéder à cette page...</h1>
            <h6>fin c'est sad la commu...</h6>
            <Link to={`/`}>
                <button style={{ cursor: 'pointer' }}>Retourer à l'accueil</button>
            </Link>
        </div>
    );
};

export default MessageForbidden;