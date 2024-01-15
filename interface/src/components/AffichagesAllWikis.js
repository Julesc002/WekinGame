import axios from "axios";
import { useEffect, useState } from "react";
import { NavLink } from 'react-router-dom';
import { API_URL } from '../config';

function AffichagesAllWikis() {
    const [wikis, setWikis] = useState([]);

    useEffect(() => {
        getAllWikiAPI();
    });

    const getAllWikiAPI = () => {
        axios.get(`${API_URL}/wikis`).then((res) => {
            const groupedWikis = groupByLetter(res.data);
            setWikis(groupedWikis);
        });
    };

    const groupByLetter = (wikis) => {
        const groupedWikis = {};
        wikis.forEach((wiki) => {
            const firstLetter = wiki.nom[0].toUpperCase();
            if (!groupedWikis[firstLetter]) {
                groupedWikis[firstLetter] = [wiki];
            } else {
                groupedWikis[firstLetter].push(wiki);
            }
        });
        return groupedWikis;
    };

    return (
        <div class="padded">
            {Object.keys(wikis).map((letter) => (
                <div key={letter}>
                    <h2>{letter}</h2>
                    <ul>
                        {wikis[letter].map((wiki) => (
                            <NavLink to={`/wiki/${wiki._id}`}>
                                <li key={wiki._id}>{wiki.nom}</li>
                            </NavLink>
                        ))}
                    </ul>
                </div>
            ))}
        </div>
    );
}

export default AffichagesAllWikis;
