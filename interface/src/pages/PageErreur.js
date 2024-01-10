import React from 'react';
import DisableBackgroundWiki from '../components/DisableBackgroundWiki';
import Header from '../components/Header';
import MessageErreur from '../components/MessageErreur';

const PageErreur = () => {
    return (
        <div>
            <DisableBackgroundWiki />
            <Header />
            <MessageErreur />
        </div>
    );
};

export default PageErreur;
