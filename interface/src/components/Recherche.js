import React, { useState } from 'react';
import axios from 'axios';
import { API_WIKI_URL } from '../config';
import _ from 'lodash';

function RechercheDeWiki() {
  const [wikis, setWikis] = useState([]);
  const [recherche, setRecherche] = useState('');

  const majRecherche = (e) => {
    setRecherche(e.target.value);
    rechercheAPI(e.target.value);
  };

  const rechercheAPI = _.debounce((query) => {
    axios.get(`${API_WIKI_URL}/search/wiki?game=` + query).then((res) => {
      const wikis = res.data.map((document) => document.nom);
      setWikis(wikis);
    });
  }, 300);

  if (wikis.length === 0 && recherche.length > 1) {
    return (
      <div>
        <input type="text" placeholder="Recherche" onChange={majRecherche}></input>
        <p>Wikis :</p>
        <p>Aucun résultat</p>
      </div>
    );
  } else {
    return (
      <div>
        <input type="text" placeholder="Recherche" onChange={majRecherche}></input>
        {recherche !== '' && <p>Wikis :</p>}
        {recherche !== '' &&
          wikis.map(function (nom, index) {
            return <p key={index}>{nom}</p>;
          })}
      </div>
    );
  }
}

export default RechercheDeWiki;
