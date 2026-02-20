import { useState, useEffect, useMemo } from 'react';

// archive component. when clicked fetches cards from backend and displays them
function Archive({ onBack }) {
    const [cards, setCards] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const cardsBySet = useMemo(() => {
        return cards.reduce((groupedCards, card) => {
            const currentSetId = card.setId || 'unknown';

            if (!groupedCards[currentSetId]) {
                groupedCards[currentSetId] = [];
            }

            groupedCards[currentSetId].push(card);
            return groupedCards;
        }, {});
    }, [cards]);

    const getSetLogoUrl = (setId) => `https://images.pokemontcg.io/${encodeURIComponent(setId)}/logo.png`;

    // connection bridge to the java springboot backend
    useEffect(() => {
        fetch('http://localhost:8080/api/cards')
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                return response.json();
            })
            .then(data => {
                setCards(data);
                setLoading(false);
            })
            .catch(err => {
                console.error("Fetch error:", err);
                setError(err.message);
                setLoading(false);
            });
    }, []);

    return (
        <div className="archive-container">
            <button className="back-btn" onClick={onBack}>← Back to Home</button>
            <h2>Card Archive</h2>

            {loading && <p>Loading cards from the Vault...</p>}
            {error && <p style={{color: 'red'}}>Error: Is your Spring Boot Backend running?</p>}

            {Object.entries(cardsBySet).map(([setId, setCards]) => (
                <section key={setId} className="set-section">
                    <div className="set-header">
                        <img
                            src={getSetLogoUrl(setId)}
                            alt={`${setId} logo`}
                            className="set-logo"
                            loading="lazy"
                            onError={(event) => {
                                event.currentTarget.style.display = 'none';
                            }}
                        />
                        <h3 className="set-title">{setId.toUpperCase()}</h3>
                    </div>

                    <div className="card-grid">
                        {setCards.map(card => (
                            <div key={card.id} className="card-item">
                                <img 
                                    src={card.imageUrl} 
                                    alt={card.name} 
                                    loading="lazy"   // lazy loading for performance
                                    width="245"     
                                    height="342"    
                                />
                                <h3>{card.name}</h3>
                                <h4>{card.setId} {card.cardNumber}</h4>
                                <p>{card.rarity}</p>
                            </div>
                        ))}
                    </div>
                </section>
            ))}
        </div>
    );
}

export default Archive;